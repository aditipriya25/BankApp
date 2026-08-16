package com.example.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<LockerAssignment> requestLocker(Authentication authentication) {
        String email = authentication.getName();
        LockerAssignment assignment = lockerAssignmentService.createCustomerRequest(email);
        return ResponseEntity.ok(assignment);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<LockerAssignment>> getPendingRequests() {
        return ResponseEntity.ok(
                lockerAssignmentService.getPendingRequests());
    }

    @PostMapping("/{assignmentId}/approve/{lockerId}")
    public ResponseEntity<LockerAssignment> approveRequest(
            @PathVariable String assignmentId,
            @PathVariable String lockerId,
            Authentication authentication) {

        String employeeEmail = authentication.getName();

        LockerAssignment assignment = lockerAssignmentService.approveRequest(
                assignmentId,
                lockerId,
                employeeEmail);

        return ResponseEntity.ok(assignment);
    }
}