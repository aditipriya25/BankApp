package com.example.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.KycRequestDto;
import com.example.demo.dto.KycReviewDto;
import com.example.demo.model.Customer;
import com.example.demo.model.KycDocument;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.service.KycService;

/**
 * KycController — REST API Controller for KYC Operations
 *
 * Base URL: /api/kyc
 *
 * ─── CUSTOMER ENDPOINTS ──────────────────────────────────────────────────────
 *   POST /api/kyc/submit           → Submit KYC documents (Aadhaar + PAN + Photo)
 *   GET  /api/kyc/status           → Check own KYC status (PENDING/APPROVED/REJECTED)
 *
 * ─── EMPLOYEE ENDPOINTS ──────────────────────────────────────────────────────
 *   GET  /api/kyc/pending          → See all PENDING KYC submissions
 *   GET  /api/kyc/all              → See ALL KYC records
 *   PUT  /api/kyc/{id}/review      → Manually approve or reject a KYC
 *
 * Security:
 *   - Customer endpoints require ROLE_CUSTOMER JWT token
 *   - Employee endpoints require ROLE_EMPLOYEE JWT token
 *   (Configured in SecurityConfig.java)
 *
 * Authentication:
 *   Spring Security injects the "Authentication" object automatically.
 *   We use authentication.getName() to get the logged-in customer's email/username.
 */
@RestController
@RequestMapping("/api/kyc")
public class KycController {


    @Autowired
    private KycService kycService;

    @Autowired
    private CustomerRepository customerRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // ENDPOINT 1: Submit KYC  (CUSTOMER only)
    // POST /api/kyc/submit
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Customer submits their KYC documents.
     *
     * The customer must send a JSON body with all Aadhaar, PAN, and photo info.
     * Example request body:
     * {
     *   "aadhaarNumber": "123456789012",
     *   "aadhaarName": "Aditi Priya",
     *   "aadhaarAddress": "123, MG Road, Bangalore",
     *   "aadhaarPhotoUrl": "https://dummy.photos/aadhaar_aditi.jpg",
     *   "panNumber": "ABCDE1234F",
     *   "panName": "Aditi Priya",
     *   "panAddress": "123, MG Road, Bangalore",
     *   "livePhotoUrl": "https://dummy.photos/selfie_aditi.jpg",
     *   "photoMatchFlag": true
     * }
     *
     * @param customerId      The customer's ID (passed as path variable)
     * @param dto             The KYC form data from the request body
     * @return                The saved KycDocument (includes auto-determined status)
     */
    @PostMapping("/submit/{customerId}")
    public ResponseEntity<KycDocument> submitKyc(
            @PathVariable String customerId,
            @RequestBody KycRequestDto dto) {

        KycDocument result = kycService.submitKyc(customerId, dto);
        return ResponseEntity.ok(result);
    }

    /**
     * Customer submits KYC using their JWT token (no customer ID needed in URL).
     * Works for both signup-flow and login-flow customers.
     */
    @PostMapping("/submit/me")
    public ResponseEntity<KycDocument> submitKycMe(
            @RequestBody KycRequestDto dto,
            Authentication authentication) {

        String email = authentication.getName();
        KycDocument result = kycService.submitKycByEmail(email, dto);
        return ResponseEntity.ok(result);
    }


    // ─────────────────────────────────────────────────────────────────────────
    // ENDPOINT 2: Check KYC Status  (CUSTOMER only)
    // GET /api/kyc/status/{customerId}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Customer checks the status of their own KYC submission.
     *
     * Returns a summary map with:
     *   - status        → PENDING / APPROVED / REJECTED / NOT_SUBMITTED
     *   - remarks       → Reason if rejected, or success message
     *   - submittedAt   → When they submitted
     *   - reviewedAt    → When employee reviewed (null if auto-validated)
     *   - aadhaarNumber → Masked confirmation of Aadhaar number
     *   - panNumber     → Masked confirmation of PAN number
     *
     * @param customerId  The customer's ID from the URL path
     * @return            A Map of KYC status details
     */
    @GetMapping("/status/{customerId}")
    public ResponseEntity<Map<String, Object>> getKycStatus(@PathVariable String customerId) {
        Map<String, Object> status = kycService.getKycStatus(customerId);
        return ResponseEntity.ok(status);
    }

    /** Returns the KYC status of the currently logged-in customer (no ID needed). */
    @GetMapping("/status/me")
    public ResponseEntity<Map<String, Object>> getMyKycStatus(Authentication authentication) {
        String email = authentication.getName();
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        Map<String, Object> status = kycService.getKycStatus(customer.getId());
        return ResponseEntity.ok(status);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ENDPOINT 3: Get All PENDING KYC  (EMPLOYEE only)
    // GET /api/kyc/pending
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Employee sees all KYC submissions that are still in PENDING status.
     *
     * @return  List of KycDocument objects with status = PENDING
     */
    @GetMapping("/pending")
    public ResponseEntity<List<KycDocument>> getAllPendingKyc() {
        List<KycDocument> pendingList = kycService.getAllPendingKyc();
        return ResponseEntity.ok(pendingList);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ENDPOINT 4: Get ALL KYC Records  (EMPLOYEE only)
    // GET /api/kyc/all
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Employee sees all KYC records regardless of status.
     * Useful for full KYC dashboard view.
     *
     * @return  List of all KycDocument objects
     */
    @GetMapping("/all")
    public ResponseEntity<List<KycDocument>> getAllKyc() {
        List<KycDocument> allKyc = kycService.getAllKyc();
        return ResponseEntity.ok(allKyc);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ENDPOINT 5: Employee Reviews / Overrides KYC Status  (EMPLOYEE only)
    // PUT /api/kyc/{id}/review
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Employee manually approves or rejects a KYC document.
     *
     * Example request body:
     * {
     *   "status": "APPROVED",
     *   "remarks": "Manually verified - documents are authentic"
     * }
     *
     * or:
     * {
     *   "status": "REJECTED",
     *   "remarks": "PAN card appears to be tampered. Please resubmit."
     * }
     *
     * @param id   The KYC document ID (from the URL path)
     * @param dto  Contains the new status and remarks
     * @return     The updated KycDocument
     */
    @PutMapping("/{id}/review")
    public ResponseEntity<KycDocument> reviewKyc(
            @PathVariable Long id,
            @RequestBody KycReviewDto dto) {

        KycDocument updated = kycService.reviewKyc(id, dto);
        return ResponseEntity.ok(updated);
    }
}
