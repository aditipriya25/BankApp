package com.example.demo.controller;

import com.example.demo.dto.RentPaymentDto;
import com.example.demo.model.LockerAssignment;
import com.example.demo.model.RentPayment;
import com.example.demo.service.RentPaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rent")
public class RentPaymentController {

    private final RentPaymentService rentPaymentService;

    public RentPaymentController(RentPaymentService rentPaymentService) {
        this.rentPaymentService = rentPaymentService;
    }

    /** Customer: Pay annual locker rent via gateway (UPI/Card/NetBanking/Offline) */
    @PostMapping("/{assignmentId}/pay")
    public ResponseEntity<RentPayment> payRent(
            @PathVariable String assignmentId,
            @RequestBody RentPaymentDto dto,
            Authentication auth) {
        return ResponseEntity.ok(rentPaymentService.payRent(assignmentId, auth.getName(), dto));
    }

    /** Customer: Retry payment (re-initiate after failure) */
    @PostMapping("/{assignmentId}/retry")
    public ResponseEntity<RentPayment> retryPayment(
            @PathVariable String assignmentId,
            @RequestBody RentPaymentDto dto,
            Authentication auth) {
        return ResponseEntity.ok(rentPaymentService.retryPayment(assignmentId, auth.getName(), dto));
    }

    /** Customer & Employee: Get rent payment history */
    @GetMapping("/{assignmentId}/history")
    public ResponseEntity<List<RentPayment>> getHistory(@PathVariable String assignmentId) {
        return ResponseEntity.ok(rentPaymentService.getRentHistory(assignmentId));
    }

    /** Employee: Get all assignments with overdue rent */
    @GetMapping("/overdue")
    public ResponseEntity<List<LockerAssignment>> getOverdue() {
        return ResponseEntity.ok(rentPaymentService.getOverdueRentAssignments());
    }
}
