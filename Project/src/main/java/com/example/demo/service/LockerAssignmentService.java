package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

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

    private final LockerAssignmentRepository lockerAssignmentRepository;
    private final CustomerRepository customerRepository;
    private final LockerRepository lockerRepository;
    private final EmployeeRepository employeeRepository;

    public LockerAssignmentService(LockerAssignmentRepository lockerAssignmentRepository,
            CustomerRepository customerRepository,
            LockerRepository lockerRepository,
            EmployeeRepository employeeRepository) {

        this.lockerAssignmentRepository = lockerAssignmentRepository;
        this.customerRepository = customerRepository;
        this.lockerRepository = lockerRepository;
        this.employeeRepository = employeeRepository;
    }

    public LockerAssignment createRequest(LockerAssignment assignment) {
        assignment.setRequestStatus("PENDING");
        assignment.setAssignedAt(null);
        return lockerAssignmentRepository.save(assignment);
    }

    public LockerAssignment createCustomerRequest(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        LockerAssignment assignment = new LockerAssignment();
        assignment.setCustomer(customer);
        assignment.setRequestStatus("PENDING");
        assignment.setAssignedAt(null);
        return lockerAssignmentRepository.save(assignment);
    }

    public List<LockerAssignment> getAllRequests() {
        return lockerAssignmentRepository.findAll();
    }

    public List<LockerAssignment> getPendingRequests() {
        return lockerAssignmentRepository.findByRequestStatus("PENDING");
    }

    public LockerAssignment approveRequest(
            String assignmentId,
            String lockerId,
            String employeeEmail) {

        LockerAssignment assignment = lockerAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Locker request not found"));

        if (!"PENDING".equals(assignment.getRequestStatus())) {
            throw new RuntimeException("Locker request is not pending");
        }

        Locker locker = lockerRepository.findByIdAndStatus(lockerId, "AVAILABLE")
                .orElseThrow(() -> new RuntimeException("Locker not found or not available"));

        Employee employee = employeeRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        assignment.setLocker(locker);
        assignment.setApprovedByEmployee(employee);
        assignment.setRequestStatus("APPROVED");
        assignment.setAssignedAt(LocalDateTime.now());

        locker.setStatus("ASSIGNED");

        lockerRepository.save(locker);

        return lockerAssignmentRepository.save(assignment);
    }

    public LockerAssignment getMyAssignment(String customerEmail) {

        Customer customer = customerRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        return lockerAssignmentRepository
                .findByCustomerIdAndRequestStatus(
                        customer.getId(),
                        "APPROVED")
                .orElse(null);
    }
}