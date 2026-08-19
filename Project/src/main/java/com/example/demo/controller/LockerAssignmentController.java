package com.example.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.LockerRequestDto;
import com.example.demo.dto.PaymentRequestDto;
import com.example.demo.model.LockerAssignment;
import com.example.demo.service.LockerAssignmentService;

@RestController
@RequestMapping("/api/locker-assignments")
public class LockerAssignmentController {
    private final LockerAssignmentService lockerAssignmentService;

    public LockerAssignmentController(LockerAssignmentService lockerAssignmentService) {
        this.lockerAssignmentService = lockerAssignmentService;
    }

    @PostMapping("/request")
    public ResponseEntity<LockerAssignment> requestLocker(
            @RequestBody LockerRequestDto request,
            Authentication authentication) {
        String email = authentication.getName();
        LockerAssignment assignment = lockerAssignmentService.createCustomerRequest(
                email, request.getLockerId());
        return ResponseEntity.ok(assignment);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<LockerAssignment>> getPendingRequests() {
        return ResponseEntity.ok(lockerAssignmentService.getPendingRequests());
    }

    @GetMapping("/approved")
    public ResponseEntity<List<LockerAssignment>> getApprovedRequests() {
        return ResponseEntity.ok(lockerAssignmentService.getApprovedRequests());
    }

    @GetMapping("/rejected")
    public ResponseEntity<List<LockerAssignment>> getRejectedRequests() {
        return ResponseEntity.ok(lockerAssignmentService.getRejectedRequests());
    }

    @PostMapping("/{assignmentId}/approve")
    public ResponseEntity<LockerAssignment> approveRequest(
            @PathVariable String assignmentId,
            Authentication authentication) {

        String employeeEmail = authentication.getName();
        LockerAssignment assignment = lockerAssignmentService.approveRequest(
                assignmentId, employeeEmail);
        return ResponseEntity.ok(assignment);
    }

    @PostMapping("/{assignmentId}/reject")
    public ResponseEntity<LockerAssignment> rejectRequest(
            @PathVariable String assignmentId,
            Authentication authentication) {

        String employeeEmail = authentication.getName();
        LockerAssignment assignment = lockerAssignmentService.rejectRequest(
                assignmentId, employeeEmail);
        return ResponseEntity.ok(assignment);
    }

    @PostMapping("/{assignmentId}/pay")
    public ResponseEntity<LockerAssignment> payForLocker(
            @PathVariable String assignmentId,
            @RequestBody PaymentRequestDto paymentRequest,
            Authentication authentication) {

        String customerEmail = authentication.getName();
        LockerAssignment assignment = lockerAssignmentService.processPayment(
                assignmentId, customerEmail, paymentRequest.getPaymentMethod());
        return ResponseEntity.ok(assignment);
    }

    /** Returns ALL active locker assignments for the logged-in customer. */
    @GetMapping("/my-assignments")
    public ResponseEntity<List<LockerAssignment>> getMyAssignments(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(lockerAssignmentService.getMyAssignments(email));
    }

    @GetMapping("/awaiting-payment")
    public ResponseEntity<List<LockerAssignment>> getAwaitingPayment() {
        return ResponseEntity.ok(lockerAssignmentService.getAwaitingPayment());
    }
}
