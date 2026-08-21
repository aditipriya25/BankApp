package com.example.demo.controller;

import com.example.demo.dto.LockerClosureDto;
import com.example.demo.model.LockerClosure;
import com.example.demo.service.LockerClosureService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/closure")
public class LockerClosureController {

    private final LockerClosureService closureService;

    public LockerClosureController(LockerClosureService closureService) {
        this.closureService = closureService;
    }

    /** Customer: Request normal locker closure (voluntary surrender / key lost) — RBI 6.1 */
    @PostMapping("/{assignmentId}/normal")
    public ResponseEntity<LockerClosure> normalClosure(
            @PathVariable String assignmentId,
            @RequestBody LockerClosureDto dto,
            Authentication auth) {
        dto.setClosureType("NORMAL");
        return ResponseEntity.ok(closureService.requestNormalClosure(assignmentId, auth.getName(), dto));
    }

    /** Nominee/Customer: Request closure due to death of locker hirer — RBI 5.2/5.3 */
    @PostMapping("/{assignmentId}/death")
    public ResponseEntity<LockerClosure> deathClosure(
            @PathVariable String assignmentId,
            @RequestBody LockerClosureDto dto,
            Authentication auth) {
        dto.setClosureType("DEATH");
        return ResponseEntity.ok(closureService.requestDeathClosure(assignmentId, auth.getName(), dto));
    }

    /** Employee: Initiate forced closure due to non-payment (3 years) — RBI 6.3.1 */
    @PostMapping("/{assignmentId}/non-payment")
    public ResponseEntity<LockerClosure> nonPaymentClosure(
            @PathVariable String assignmentId,
            Authentication auth) {
        return ResponseEntity.ok(closureService.initiateNonPaymentClosure(assignmentId, auth.getName()));
    }

    /** Employee: Initiate law enforcement/court-order closure — RBI 6.2 */
    @PostMapping("/{assignmentId}/law-enforcement")
    public ResponseEntity<LockerClosure> lawEnforcementClosure(
            @PathVariable String assignmentId,
            @RequestBody LockerClosureDto dto,
            Authentication auth) {
        dto.setClosureType("LAW_ENFORCEMENT");
        return ResponseEntity.ok(closureService.initiateLawEnforcementClosure(assignmentId, auth.getName(), dto));
    }

    /**
     * Employee: Approve a customer's REQUESTED closure.
     * Immediately sets locker to AVAILABLE and assignment to CLOSED.
     */
    @PutMapping("/{closureId}/approve")
    public ResponseEntity<LockerClosure> approveClosure(
            @PathVariable String closureId,
            Authentication auth) {
        return ResponseEntity.ok(closureService.approveClosure(closureId, auth.getName()));
    }

    /**
     * Employee: Reject a customer's REQUESTED closure.
     * Reverts closure status; locker remains active.
     */
    @PutMapping("/{closureId}/reject")
    public ResponseEntity<LockerClosure> rejectClosure(
            @PathVariable String closureId,
            @RequestBody(required = false) Map<String, String> body,
            Authentication auth) {
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(closureService.rejectClosure(closureId, auth.getName(), reason));
    }

    /** Employee: Complete a NON_PAYMENT/LAW_ENFORCEMENT closure (add inventory, witnesses, video) */
    @PutMapping("/{closureId}/complete")
    public ResponseEntity<LockerClosure> completeClosure(
            @PathVariable String closureId,
            @RequestBody LockerClosureDto dto,
            Authentication auth) {
        return ResponseEntity.ok(closureService.completeClosure(closureId, auth.getName(), dto));
    }

    /** Get closure status by closure ID */
    @GetMapping("/{closureId}/status")
    public ResponseEntity<LockerClosure> getStatus(@PathVariable String closureId) {
        return ResponseEntity.ok(closureService.getClosureStatus(closureId));
    }

    /** Get closure record by assignment ID */
    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<LockerClosure> getByAssignment(@PathVariable String assignmentId) {
        return ResponseEntity.ok(closureService.getClosureByAssignment(assignmentId));
    }

    /** Employee: List all pending closures */
    @GetMapping("/pending")
    public ResponseEntity<List<LockerClosure>> getPending() {
        return ResponseEntity.ok(closureService.getPendingClosures());
    }

    /** Employee: List all closures */
    @GetMapping("/all")
    public ResponseEntity<List<LockerClosure>> getAll() {
        return ResponseEntity.ok(closureService.getAllClosures());
    }
}
