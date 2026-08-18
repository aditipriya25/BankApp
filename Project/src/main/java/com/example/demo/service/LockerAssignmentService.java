package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.Customer;
import com.example.demo.model.Employee;
import com.example.demo.model.Locker;
import com.example.demo.model.LockerAssignment;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.LockerAssignmentRepository;
import com.example.demo.repository.LockerRepository;

@Service
public class LockerAssignmentService {

    private static final List<String> ACTIVE_REQUEST_STATUSES = Arrays.asList(
            "PENDING", "APPROVED", "PAID");

    private final LockerAssignmentRepository lockerAssignmentRepository;
    private final CustomerRepository customerRepository;
    private final LockerRepository lockerRepository;
    private final EmployeeRepository employeeRepository;

    @Value("${locker.payment.deadline.days:7}")
    private int paymentDeadlineDays;

    public LockerAssignmentService(LockerAssignmentRepository lockerAssignmentRepository,
            CustomerRepository customerRepository,
            LockerRepository lockerRepository,
            EmployeeRepository employeeRepository) {

        this.lockerAssignmentRepository = lockerAssignmentRepository;
        this.customerRepository = customerRepository;
        this.lockerRepository = lockerRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public LockerAssignment createCustomerRequest(String email, String lockerId) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        lockerAssignmentRepository.findByCustomerIdAndRequestStatusIn(
                customer.getId(), ACTIVE_REQUEST_STATUSES)
                .ifPresent(existing -> {
                    throw new RuntimeException("You already have an active locker request");
                });

        Locker locker = lockerRepository.findByIdAndStatus(lockerId, "AVAILABLE")
                .orElseThrow(() -> new RuntimeException("Locker not found or not available"));

        locker.setStatus("RESERVED");
        lockerRepository.save(locker);

        LockerAssignment assignment = new LockerAssignment();
        assignment.setCustomer(customer);
        assignment.setLocker(locker);
        assignment.setRequestStatus("PENDING");
        assignment.setPaymentStatus("NOT_REQUIRED");
        assignment.setAssignedAt(null);

        return lockerAssignmentRepository.save(assignment);
    }

    public List<LockerAssignment> getPendingRequests() {
        return lockerAssignmentRepository.findByRequestStatus("PENDING");
    }

    @Transactional
    public LockerAssignment approveRequest(String assignmentId, String employeeEmail) {
        LockerAssignment assignment = lockerAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Locker request not found"));

        if (!"PENDING".equals(assignment.getRequestStatus())) {
            throw new RuntimeException("Locker request is not pending");
        }

        Employee employee = employeeRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        assignment.setApprovedByEmployee(employee);
        assignment.setRequestStatus("APPROVED");
        assignment.setPaymentStatus("PENDING");
        assignment.setPaymentDueDate(LocalDateTime.now().plusDays(paymentDeadlineDays));

        return lockerAssignmentRepository.save(assignment);
    }

    @Transactional
    public LockerAssignment rejectRequest(String assignmentId, String employeeEmail) {
        LockerAssignment assignment = lockerAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Locker request not found"));

        if (!"PENDING".equals(assignment.getRequestStatus())) {
            throw new RuntimeException("Locker request is not pending");
        }

        Employee employee = employeeRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        releaseLocker(assignment.getLocker());

        assignment.setApprovedByEmployee(employee);
        assignment.setRequestStatus("REJECTED");
        assignment.setPaymentStatus("NOT_REQUIRED");

        return lockerAssignmentRepository.save(assignment);
    }

    @Transactional
    public LockerAssignment processPayment(String assignmentId, String customerEmail, String paymentMethod) {
        LockerAssignment assignment = lockerAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Locker assignment not found"));

        if (!assignment.getCustomer().getEmail().equals(customerEmail)) {
            throw new RuntimeException("You are not authorized to pay for this locker");
        }

        if (!"APPROVED".equals(assignment.getRequestStatus())) {
            throw new RuntimeException("Locker request must be approved before payment");
        }

        if ("PAID".equals(assignment.getPaymentStatus())) {
            throw new RuntimeException("Payment has already been completed");
        }

        expireIfOverdue(assignment);
        if ("EXPIRED".equals(assignment.getRequestStatus())) {
            throw new RuntimeException("Payment deadline has passed. Please submit a new locker request");
        }

        String normalizedMethod = paymentMethod.toUpperCase();
        if (!"ONLINE".equals(normalizedMethod) && !"OFFLINE".equals(normalizedMethod)) {
            throw new RuntimeException("Payment method must be ONLINE or OFFLINE");
        }

        assignment.setPaymentMethod(normalizedMethod);
        assignment.setPaymentStatus("PAID");
        assignment.setRequestStatus("PAID");
        assignment.setPaidAt(LocalDateTime.now());
        assignment.setAssignedAt(LocalDateTime.now());

        Locker locker = assignment.getLocker();
        locker.setStatus("ASSIGNED");
        lockerRepository.save(locker);

        return lockerAssignmentRepository.save(assignment);
    }

    public LockerAssignment getMyAssignment(String customerEmail) {
        Customer customer = customerRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        List<LockerAssignment> assignments = lockerAssignmentRepository.findByCustomer_Email(customerEmail);
        for (LockerAssignment assignment : assignments) {
            expireIfOverdue(assignment);
            if (ACTIVE_REQUEST_STATUSES.contains(assignment.getRequestStatus())
                    || "PAID".equals(assignment.getRequestStatus())) {
                return assignment;
            }
        }
        return null;
    }

    public List<LockerAssignment> getAwaitingPayment() {
        List<LockerAssignment> approved = lockerAssignmentRepository.findByRequestStatus("APPROVED");
        approved.forEach(this::expireIfOverdue);
        return lockerAssignmentRepository.findByRequestStatus("APPROVED");
    }

    private void expireIfOverdue(LockerAssignment assignment) {
        if (!"APPROVED".equals(assignment.getRequestStatus())) {
            return;
        }
        if (assignment.getPaymentDueDate() != null
                && LocalDateTime.now().isAfter(assignment.getPaymentDueDate())) {
            assignment.setRequestStatus("EXPIRED");
            assignment.setPaymentStatus("EXPIRED");
            releaseLocker(assignment.getLocker());
            lockerAssignmentRepository.save(assignment);
        }
    }

    private void releaseLocker(Locker locker) {
        if (locker != null) {
            locker.setStatus("AVAILABLE");
            lockerRepository.save(locker);
        }
    }
}
