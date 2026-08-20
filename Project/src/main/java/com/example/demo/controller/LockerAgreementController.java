package com.example.demo.controller;

import com.example.demo.model.LockerAgreement;
import com.example.demo.service.LockerAgreementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agreements")
public class LockerAgreementController {

    private final LockerAgreementService agreementService;

    public LockerAgreementController(LockerAgreementService agreementService) {
        this.agreementService = agreementService;
    }

    /** Employee: Generate locker agreement on assignment (RBI 2.1.2) */
    @PostMapping("/{assignmentId}")
    public ResponseEntity<LockerAgreement> createAgreement(
            @PathVariable String assignmentId,
            Authentication auth) {
        return ResponseEntity.ok(agreementService.createAgreement(assignmentId, auth.getName()));
    }

    /** Customer or Employee: Get the agreement for an assignment */
    @GetMapping("/{assignmentId}")
    public ResponseEntity<LockerAgreement> getAgreement(@PathVariable String assignmentId) {
        return ResponseEntity.ok(agreementService.getAgreement(assignmentId));
    }

    /** Customer: Digitally sign/accept the agreement */
    @PostMapping("/{assignmentId}/sign")
    public ResponseEntity<LockerAgreement> signAgreement(
            @PathVariable String assignmentId,
            Authentication auth) {
        return ResponseEntity.ok(agreementService.customerSignAgreement(assignmentId, auth.getName()));
    }

    /** Employee: Renew agreement (RBI 2.1.1: renewal due by Jan 1, 2023) */
    @PostMapping("/{assignmentId}/renew")
    public ResponseEntity<LockerAgreement> renewAgreement(
            @PathVariable String assignmentId,
            Authentication auth) {
        return ResponseEntity.ok(agreementService.renewAgreement(assignmentId, auth.getName()));
    }
}
