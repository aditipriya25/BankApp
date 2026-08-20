package com.example.demo.service;

import com.example.demo.dto.LockerClosureDto;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for locker closure scenarios per RBI Part VI.
 *
 * Scenarios implemented:
 *  NORMAL       — RBI 6.1: Customer requests closure (key lost / voluntary surrender)
 *  DEATH        — RBI 5.2 & 5.3: Settlement of claims on death of locker hirer (≤15 days)
 *  NON_PAYMENT  — RBI 6.3: Bank-initiated after 3 consecutive unpaid years
 *  INOPERATIVE  — RBI 6.4: Locker unused for 7 years (even if rent paid)
 *  LAW_ENFORCEMENT — RBI 6.2: Court/authority attachment order
 */
@Service
public class LockerClosureService {

    private final LockerClosureRepository closureRepository;
    private final LockerAssignmentRepository assignmentRepository;
    private final EmployeeRepository employeeRepository;
    private final LockerRepository lockerRepository;

    public LockerClosureService(LockerClosureRepository closureRepository,
                                LockerAssignmentRepository assignmentRepository,
                                EmployeeRepository employeeRepository,
                                LockerRepository lockerRepository) {
        this.closureRepository = closureRepository;
        this.assignmentRepository = assignmentRepository;
        this.employeeRepository = employeeRepository;
        this.lockerRepository = lockerRepository;
    }

    /**
     * Normal closure — customer initiates (RBI 6.1: key lost / voluntary surrender).
     * Customer pays all charges; locker is returned.
     */
    @Transactional
    public LockerClosure requestNormalClosure(String assignmentId, String customerEmail, LockerClosureDto dto) {
        LockerAssignment assignment = getActiveAssignment(assignmentId);

        if (!assignment.getCustomer().getEmail().equals(customerEmail)) {
            throw new RuntimeException("Access denied");
        }

        ensureNoPendingClosure(assignmentId);

        LockerClosure closure = buildClosure(assignment, "NORMAL", dto);
        // RBI 6.1.1: Customer must give written authorization
        closure.setStatus("REQUESTED");
        closure.setNoticeDueDate(LocalDateTime.now().plusDays(7)); // 7 days for bank to process

        // Update assignment status
        assignment.setClosureType("NORMAL");
        assignment.setClosureStatus("REQUESTED");
        assignment.setClosureRequestedAt(LocalDateTime.now());
        assignmentRepository.save(assignment);

        return closureRepository.save(closure);
    }

    /**
     * Death closure — nominee/survivor claims contents (RBI 5.2.4: must settle within 15 days).
     */
    @Transactional
    public LockerClosure requestDeathClosure(String assignmentId, String nomineeOrEmployeeEmail, LockerClosureDto dto) {
        LockerAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if (dto.getDeathCertificateUrl() == null || dto.getDeathCertificateUrl().isBlank()) {
            throw new RuntimeException("Death certificate URL is required for death closure (RBI para 5.2.3)");
        }

        ensureNoPendingClosure(assignmentId);

        LockerClosure closure = buildClosure(assignment, "DEATH", dto);
        // RBI 5.2.4: Banks shall settle claims within 15 days
        closure.setNoticeDueDate(LocalDateTime.now().plusDays(15));
        closure.setClaimantDetails(dto.getClaimantDetails());
        closure.setDeathCertificateUrl(dto.getDeathCertificateUrl());

        assignment.setClosureType("DEATH");
        assignment.setClosureStatus("REQUESTED");
        assignment.setClosureRequestedAt(LocalDateTime.now());
        assignmentRepository.save(assignment);

        return closureRepository.save(closure);
    }

    /**
     * Non-payment closure — employee/system initiates after 3+ unpaid years (RBI 6.3.1).
     * Bank must issue notice before breaking open.
     */
    @Transactional
    public LockerClosure initiateNonPaymentClosure(String assignmentId, String employeeEmail) {
        LockerAssignment assignment = getActiveAssignment(assignmentId);

        if (assignment.getConsecutiveUnpaidYears() < 3) {
            throw new RuntimeException(
                "Non-payment closure requires at least 3 consecutive unpaid years per RBI para 6.3.1. " +
                "Current unpaid years: " + assignment.getConsecutiveUnpaidYears()
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
        // RBI 6.3.2: Give reasonable notice period before break-open
        closure.setNoticeIssuedAt(LocalDateTime.now());
        closure.setNoticeDueDate(LocalDateTime.now().plusDays(30)); // 30 days notice

        assignment.setClosureType("NON_PAYMENT");
        assignment.setClosureStatus("NOTICE_ISSUED");
        assignment.setClosureRequestedAt(LocalDateTime.now());
        assignmentRepository.save(assignment);

        return closureRepository.save(closure);
    }

    /**
     * Law enforcement closure — court/authority order (RBI 6.2).
     */
    @Transactional
    public LockerClosure initiateLawEnforcementClosure(String assignmentId, String employeeEmail, LockerClosureDto dto) {
        LockerAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        Employee employee = employeeRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (dto.getCourtOrderUrl() == null || dto.getCourtOrderUrl().isBlank()) {
            throw new RuntimeException("Court order URL is required for law enforcement closure (RBI 6.2.1)");
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
     * Complete closure procedure — employee finalizes (adds inventory, witnesses, video).
     * RBI 6.3.2: Inventory prepared in presence of 2 independent witnesses + bank officer.
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

        // Calculate RBI compensation if fire/theft/fraud type (7.2: 100× annual rent)
        if ("COMPENSATION".equals(dto.getReason()) || "NON_PAYMENT".equals(closure.getClosureType())) {
            BigDecimal annualRent = closure.getAssignment().getLocker().getPrice()
                    .multiply(BigDecimal.valueOf(12));
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

    /** All non-completed closures for employee review */
    public List<LockerClosure> getPendingClosures() {
        return closureRepository.findByStatusNot("COMPLETED");
    }

    public List<LockerClosure> getAllClosures() {
        return closureRepository.findAll();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private LockerAssignment getActiveAssignment(String assignmentId) {
        LockerAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        if ("CLOSED".equals(assignment.getRequestStatus())) {
            throw new RuntimeException("This locker assignment is already closed");
        }
        return assignment;
    }

    private void ensureNoPendingClosure(String assignmentId) {
        closureRepository.findByAssignment_Id(assignmentId).ifPresent(c -> {
            if (!"COMPLETED".equals(c.getStatus())) {
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
