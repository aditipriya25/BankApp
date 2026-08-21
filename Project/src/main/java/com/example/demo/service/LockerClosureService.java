package com.example.demo.service;

import com.example.demo.dto.LockerClosureDto;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for locker closure scenarios per RBI Part VI.
 *
 * Workflow for NORMAL / DEATH closures:
 *   1. Customer requests closure → status = REQUESTED
 *   2. Employee APPROVES or REJECTS
 *      - APPROVED → locker immediately set to AVAILABLE, assignment = CLOSED
 *      - REJECTED → closure deleted, assignment back to normal
 *
 * NON_PAYMENT / LAW_ENFORCEMENT closures are employee-initiated and go directly to completion.
 */
@Service
public class LockerClosureService {

    private final LockerClosureRepository closureRepository;
    private final LockerAssignmentRepository assignmentRepository;
    private final EmployeeRepository employeeRepository;
    private final LockerRepository lockerRepository;
    private final NotificationService notificationService;

    public LockerClosureService(LockerClosureRepository closureRepository,
                                LockerAssignmentRepository assignmentRepository,
                                EmployeeRepository employeeRepository,
                                LockerRepository lockerRepository,
                                NotificationService notificationService) {
        this.closureRepository   = closureRepository;
        this.assignmentRepository = assignmentRepository;
        this.employeeRepository  = employeeRepository;
        this.lockerRepository    = lockerRepository;
        this.notificationService = notificationService;
    }

    // ── Customer-initiated closures ───────────────────────────────────────────

    /**
     * Normal closure — customer initiates (RBI 6.1: key lost / voluntary surrender).
     * Stays REQUESTED until employee approves.
     */
    @Transactional
    public LockerClosure requestNormalClosure(String assignmentId, String customerEmail, LockerClosureDto dto) {
        LockerAssignment assignment = getActiveAssignment(assignmentId);

        if (!assignment.getCustomer().getEmail().equals(customerEmail)) {
            throw new RuntimeException("Access denied");
        }
        ensureNoPendingClosure(assignmentId);

        LockerClosure closure = buildClosure(assignment, "NORMAL", dto);
        closure.setStatus("REQUESTED");
        closure.setNoticeDueDate(LocalDateTime.now().plusDays(7));

        assignment.setClosureType("NORMAL");
        assignment.setClosureStatus("REQUESTED");
        assignment.setClosureRequestedAt(LocalDateTime.now());
        assignmentRepository.save(assignment);

        LockerClosure saved = closureRepository.save(closure);

        // Notify customer
        notificationService.createNotification(
                customerEmail, "CUSTOMER",
                "Closure Request Submitted",
                "Your locker closure request for Locker " +
                        assignment.getLocker().getLockerNumber() + " has been submitted. Awaiting employee approval.",
                "CLOSURE_REQUESTED"
        );

        return saved;
    }

    /**
     * Death closure — nominee/customer files (RBI 5.2/5.3).
     * Stays REQUESTED until employee approves.
     */
    @Transactional
    public LockerClosure requestDeathClosure(String assignmentId, String requesterEmail, LockerClosureDto dto) {
        LockerAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if (dto.getDeathCertificateUrl() == null || dto.getDeathCertificateUrl().isBlank()) {
            throw new RuntimeException("Death certificate URL is required (RBI para 5.2.3)");
        }
        ensureNoPendingClosure(assignmentId);

        LockerClosure closure = buildClosure(assignment, "DEATH", dto);
        closure.setStatus("REQUESTED");
        closure.setNoticeDueDate(LocalDateTime.now().plusDays(15));
        closure.setClaimantDetails(dto.getClaimantDetails());
        closure.setDeathCertificateUrl(dto.getDeathCertificateUrl());

        assignment.setClosureType("DEATH");
        assignment.setClosureStatus("REQUESTED");
        assignment.setClosureRequestedAt(LocalDateTime.now());
        assignmentRepository.save(assignment);

        return closureRepository.save(closure);
    }

    // ── Employee approval / rejection of customer-initiated closures ──────────

    /**
     * Employee approves a REQUESTED closure.
     * Immediately sets locker to AVAILABLE and assignment to CLOSED.
     * Notifies the customer.
     */
    @Transactional
    public LockerClosure approveClosure(String closureId, String employeeEmail) {
        LockerClosure closure = closureRepository.findById(closureId)
                .orElseThrow(() -> new RuntimeException("Closure record not found"));

        if (!"REQUESTED".equals(closure.getStatus())) {
            throw new RuntimeException("Only REQUESTED closures can be approved");
        }

        Employee employee = employeeRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        closure.setStatus("COMPLETED");
        closure.setProcessedByEmployee(employee);
        closure.setCompletedAt(LocalDateTime.now());

        LockerAssignment assignment = closure.getAssignment();
        assignment.setClosureStatus("COMPLETED");
        assignment.setClosureCompletedAt(LocalDateTime.now());
        assignment.setRequestStatus("CLOSED");
        assignmentRepository.save(assignment);

        // Release the locker — immediately AVAILABLE
        Locker locker = assignment.getLocker();
        locker.setStatus("AVAILABLE");
        lockerRepository.save(locker);

        LockerClosure saved = closureRepository.save(closure);

        // Notify customer
        notificationService.createNotification(
                assignment.getCustomer().getEmail(), "CUSTOMER",
                "Locker Closure Approved",
                "Your closure request for Locker " + locker.getLockerNumber() +
                        " has been approved. The locker is now closed and your account has been updated.",
                "CLOSURE_APPROVED"
        );

        return saved;
    }

    /**
     * Employee rejects a REQUESTED closure.
     * Reverts the assignment closure status. Notifies the customer.
     */
    @Transactional
    public LockerClosure rejectClosure(String closureId, String employeeEmail, String reason) {
        LockerClosure closure = closureRepository.findById(closureId)
                .orElseThrow(() -> new RuntimeException("Closure record not found"));

        if (!"REQUESTED".equals(closure.getStatus())) {
            throw new RuntimeException("Only REQUESTED closures can be rejected");
        }

        Employee employee = employeeRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        closure.setStatus("REJECTED");
        closure.setProcessedByEmployee(employee);
        closure.setReason(reason != null ? reason : closure.getReason());

        LockerAssignment assignment = closure.getAssignment();
        assignment.setClosureStatus("NONE");
        assignment.setClosureType(null);
        assignmentRepository.save(assignment);

        LockerClosure saved = closureRepository.save(closure);

        // Notify customer
        notificationService.createNotification(
                assignment.getCustomer().getEmail(), "CUSTOMER",
                "Locker Closure Rejected",
                "Your closure request for Locker " + assignment.getLocker().getLockerNumber() +
                        " has been rejected. Reason: " + (reason != null ? reason : "No reason provided") +
                        ". Please contact your branch for further assistance.",
                "CLOSURE_REJECTED"
        );

        return saved;
    }

    // ── Employee-initiated closures ───────────────────────────────────────────

    /** Non-payment closure — employee/system initiates after 3+ unpaid years (RBI 6.3.1). */
    @Transactional
    public LockerClosure initiateNonPaymentClosure(String assignmentId, String employeeEmail) {
        LockerAssignment assignment = getActiveAssignment(assignmentId);

        if (assignment.getConsecutiveUnpaidYears() < 3) {
            throw new RuntimeException(
                "Non-payment closure requires at least 3 consecutive unpaid years (RBI 6.3.1). " +
                "Current: " + assignment.getConsecutiveUnpaidYears()
            );
        }
        ensureNoPendingClosure(assignmentId);

        Employee employee = employeeRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        LockerClosure closure = new LockerClosure();
        closure.setAssignment(assignment);
        closure.setClosureType("NON_PAYMENT");
        closure.setStatus("NOTICE_ISSUED");
        closure.setProcessedByEmployee(employee);
        closure.setReason("Rent unpaid for " + assignment.getConsecutiveUnpaidYears() + " consecutive years");
        closure.setNoticeIssuedAt(LocalDateTime.now());
        closure.setNoticeDueDate(LocalDateTime.now().plusDays(30));

        assignment.setClosureType("NON_PAYMENT");
        assignment.setClosureStatus("NOTICE_ISSUED");
        assignment.setClosureRequestedAt(LocalDateTime.now());
        assignmentRepository.save(assignment);

        notificationService.createNotification(
                assignment.getCustomer().getEmail(), "CUSTOMER",
                "Forced Closure Notice",
                "Your locker " + assignment.getLocker().getLockerNumber() +
                        " has been issued a forced closure notice due to " +
                        assignment.getConsecutiveUnpaidYears() + " years of unpaid rent (RBI 6.3.1). " +
                        "Please clear dues within 30 days or the locker will be closed.",
                "CLOSURE_REQUESTED"
        );

        return closureRepository.save(closure);
    }

    /** Law enforcement closure — court/authority order (RBI 6.2). */
    @Transactional
    public LockerClosure initiateLawEnforcementClosure(String assignmentId, String employeeEmail, LockerClosureDto dto) {
        LockerAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        Employee employee = employeeRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (dto.getCourtOrderUrl() == null || dto.getCourtOrderUrl().isBlank()) {
            throw new RuntimeException("Court order URL is required (RBI 6.2.1)");
        }

        LockerClosure closure = buildClosure(assignment, "LAW_ENFORCEMENT", dto);
        closure.setProcessedByEmployee(employee);
        closure.setCourtOrderUrl(dto.getCourtOrderUrl());

        assignment.setClosureType("LAW_ENFORCEMENT");
        assignment.setClosureStatus("IN_PROGRESS");
        assignmentRepository.save(assignment);

        return closureRepository.save(closure);
    }

    /**
     * Complete closure (NON_PAYMENT / LAW_ENFORCEMENT only) — employee finalizes with inventory, witnesses, video.
     * NORMAL / DEATH closures are approved directly via approveClosure().
     */
    @Transactional
    public LockerClosure completeClosure(String closureId, String employeeEmail, LockerClosureDto dto) {
        LockerClosure closure = closureRepository.findById(closureId)
                .orElseThrow(() -> new RuntimeException("Closure record not found"));

        Employee employee = employeeRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        closure.setStatus("COMPLETED");
        closure.setProcessedByEmployee(employee);
        closure.setCompletedAt(LocalDateTime.now());
        closure.setInventoryDetails(dto.getInventoryDetails());
        closure.setWitness1Name(dto.getWitness1Name());
        closure.setWitness2Name(dto.getWitness2Name());
        closure.setVideoUrl(dto.getVideoUrl());
        if (dto.getNewspaperNoticeDetails() != null) {
            closure.setNewspaperNoticeDetails(dto.getNewspaperNoticeDetails());
        }

        // RBI 7.2 compensation if applicable
        if ("NON_PAYMENT".equals(closure.getClosureType())) {
            BigDecimal annualRent = closure.getAssignment().getLocker().getPrice();
            closure.setCompensationAmount(annualRent.multiply(BigDecimal.valueOf(100)));
        }

        // Release the locker
        LockerAssignment assignment = closure.getAssignment();
        assignment.setClosureStatus("COMPLETED");
        assignment.setClosureCompletedAt(LocalDateTime.now());
        assignment.setRequestStatus("CLOSED");
        assignmentRepository.save(assignment);

        Locker locker = assignment.getLocker();
        locker.setStatus("AVAILABLE");
        lockerRepository.save(locker);

        return closureRepository.save(closure);
    }

    public LockerClosure getClosureStatus(String closureId) {
        return closureRepository.findById(closureId)
                .orElseThrow(() -> new RuntimeException("Closure record not found"));
    }

    public LockerClosure getClosureByAssignment(String assignmentId) {
        return closureRepository.findByAssignment_Id(assignmentId)
                .orElseThrow(() -> new RuntimeException("No closure record for this assignment"));
    }

    public List<LockerClosure> getPendingClosures() {
        return closureRepository.findByStatusNot("COMPLETED");
    }

    public List<LockerClosure> getAllClosures() {
        return closureRepository.findAll();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private LockerAssignment getActiveAssignment(String assignmentId) {
        LockerAssignment a = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        if ("CLOSED".equals(a.getRequestStatus())) {
            throw new RuntimeException("This locker assignment is already closed");
        }
        return a;
    }

    private void ensureNoPendingClosure(String assignmentId) {
        closureRepository.findByAssignment_Id(assignmentId).ifPresent(c -> {
            if (!"COMPLETED".equals(c.getStatus()) && !"REJECTED".equals(c.getStatus())) {
                throw new RuntimeException("A closure request is already pending for this locker");
            }
        });
    }

    private LockerClosure buildClosure(LockerAssignment assignment, String type, LockerClosureDto dto) {
        LockerClosure closure = new LockerClosure();
        closure.setAssignment(assignment);
        closure.setClosureType(type);
        closure.setStatus("REQUESTED");
        closure.setReason(dto.getReason());
        if (dto.getInventoryDetails() != null) closure.setInventoryDetails(dto.getInventoryDetails());
        if (dto.getWitness1Name() != null)    closure.setWitness1Name(dto.getWitness1Name());
        if (dto.getWitness2Name() != null)    closure.setWitness2Name(dto.getWitness2Name());
        if (dto.getVideoUrl() != null)         closure.setVideoUrl(dto.getVideoUrl());
        return closure;
    }
}
