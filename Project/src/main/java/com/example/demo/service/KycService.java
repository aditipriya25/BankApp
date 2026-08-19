package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.KycRequestDto;
import com.example.demo.dto.KycReviewDto;
import com.example.demo.model.Customer;
import com.example.demo.model.KycDocument;
import com.example.demo.model.KycStatus;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.KycDocumentRepository;

/**
 * KycService — Business Logic for KYC Verification
 *
 * This service handles everything related to KYC:
 *   1. submitKyc()      → Customer submits Aadhaar, PAN, photo → runs dummy checks
 *   2. autoValidate()   → The actual dummy check logic (name, address, photo match)
 *   3. getKycStatus()   → Customer checks their own KYC status
 *   4. getAllPending()   → Employee sees all PENDING KYC submissions
 *   5. getAllKyc()       → Employee sees ALL KYC records
 *   6. reviewKyc()      → Employee manually sets APPROVED or REJECTED
 */
@Service
public class KycService {

    @Autowired
    private KycDocumentRepository kycDocumentRepository;

    @Autowired
    private CustomerRepository customerRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // METHOD 1: submitKyc
    // Called by: CUSTOMER via POST /api/kyc/submit
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Accepts KYC submission from a customer, maps the DTO to an entity,
     * runs dummy validation, sets the status, and saves to DB.
     *
     * If the customer already has a KYC record → updates it (allows re-submission).
     * If they don't have one yet              → creates a new one.
     *
     * @param customerId  The ID of the customer submitting KYC
     * @param dto         The KYC form data (Aadhaar, PAN, photo info)
     * @return            The saved KycDocument with the auto-determined status
     */
    public KycDocument submitKyc(String customerId, KycRequestDto dto) {

        // Step 1: Find the customer in the DB (throw error if not found)
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));

        // Step 2: Check if this customer already has a KYC document
        //         If yes, we update the existing one (re-submission allowed).
        //         If no, we create a fresh KycDocument object.
        KycDocument kycDocument = kycDocumentRepository
                .findByCustomerId(customerId)
                .orElse(new KycDocument());  // create new if not exists

        // Step 3: Set the customer reference (FK: customer_id in KYC_DOCUMENT table)
        kycDocument.setCustomer(customer);

        // Step 4: Copy all the Aadhaar fields from the DTO into the entity
        kycDocument.setAadhaarNumber(dto.getAadhaarNumber());
        kycDocument.setAadhaarName(dto.getAadhaarName());
        kycDocument.setAadhaarAddress(dto.getAadhaarAddress());
        kycDocument.setAadhaarPhotoUrl(dto.getAadhaarPhotoUrl());

        // Step 5: Copy all the PAN fields from the DTO into the entity
        kycDocument.setPanNumber(dto.getPanNumber());
        kycDocument.setPanName(dto.getPanName());
        kycDocument.setPanAddress(dto.getPanAddress());

        // Step 6: Copy the live/selfie photo URL
        kycDocument.setLivePhotoUrl(dto.getLivePhotoUrl());

        // Step 7: Copy the dummy photo match flag
        kycDocument.setPhotoMatchFlag(dto.isPhotoMatchFlag());

        // Step 8: Record the submission timestamp
        kycDocument.setSubmittedAt(LocalDateTime.now());

        // Step 9: Clear any old reviewed timestamp (in case of re-submission)
        kycDocument.setReviewedAt(null);

        // Step 10: *** RUN DUMMY AUTO-VALIDATION ***
        //          This method checks name, address, and photo → sets APPROVED or REJECTED
        autoValidate(kycDocument);

        // Step 11: Also update the kycStatus on the Customer entity itself
        customer.setKycStatus(kycDocument.getKycStatus());
        customerRepository.save(customer);

        // Step 12: Save the KYC document to the DB and return it
        return kycDocumentRepository.save(kycDocument);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // METHOD 2: autoValidate (THE DUMMY VALIDATION ENGINE)
    // Called internally by submitKyc()
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * This is the heart of the dummy KYC system.
     * It performs three checks and decides APPROVED or REJECTED.
     *
     * CHECK 1: Name Match
     *   aadhaarName must equal panName (case-insensitive, trimmed)
     *   Real equivalent: UIDAI API returns the name → compare with PAN name
     *
     * CHECK 2: Address Match
     *   aadhaarAddress must equal panAddress (case-insensitive, trimmed)
     *   Real equivalent: Aadhaar address vs PAN registered address
     *
     * CHECK 3: Photo Match
     *   photoMatchFlag must be true
     *   Real equivalent: AI face-recognition comparing Aadhaar photo with live selfie
     *
     * Decision logic:
     *   ALL 3 checks pass → APPROVED  ✅
     *   ANY check fails   → REJECTED  ❌  (with a reason in remarks)
     *
     * @param doc  The KycDocument to validate (modified in-place)
     */
    private void autoValidate(KycDocument doc) {

        // Track which checks failed (for the remarks/reason message)
        StringBuilder failureReasons = new StringBuilder();
        boolean allPassed = true;

        // ── CHECK 1: Name must match ──────────────────────────────────────────
        String aadhaarNameTrimmed = (doc.getAadhaarName() != null) ? doc.getAadhaarName().trim() : "";
        String panNameTrimmed     = (doc.getPanName() != null)     ? doc.getPanName().trim()     : "";

        if (!aadhaarNameTrimmed.equalsIgnoreCase(panNameTrimmed)) {
            // Names don't match → record failure
            failureReasons.append("Name mismatch: Aadhaar name '")
                          .append(aadhaarNameTrimmed)
                          .append("' does not match PAN name '")
                          .append(panNameTrimmed)
                          .append("'. ");
            allPassed = false;
        }

        // ── CHECK 2: Address must match ───────────────────────────────────────
        String aadhaarAddressTrimmed = (doc.getAadhaarAddress() != null) ? doc.getAadhaarAddress().trim() : "";
        String panAddressTrimmed     = (doc.getPanAddress() != null)     ? doc.getPanAddress().trim()     : "";

        if (!aadhaarAddressTrimmed.equalsIgnoreCase(panAddressTrimmed)) {
            // Addresses don't match → record failure
            failureReasons.append("Address mismatch: Aadhaar address does not match PAN address. ");
            allPassed = false;
        }

        // ── CHECK 3: Photo must match ─────────────────────────────────────────
        if (!doc.isPhotoMatchFlag()) {
            // Photo flag is false → record failure
            failureReasons.append("Photo mismatch: Live photo does not match Aadhaar photo. ");
            allPassed = false;
        }

        // ── FINAL DECISION ────────────────────────────────────────────────────
        if (allPassed) {
            doc.setKycStatus(KycStatus.APPROVED);
            doc.setRemarks("All checks passed: Name, Address, and Photo verified successfully.");
        } else {
            doc.setKycStatus(KycStatus.REJECTED);
            doc.setRemarks("KYC Rejected. Reason(s): " + failureReasons.toString().trim());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // METHOD 3: getKycStatus
    // Called by: CUSTOMER via GET /api/kyc/status
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the KYC document for a given customer.
     * The customer uses this to check the result of their submission.
     *
     * @param customerId  The customer's ID
     * @return            A Map with: status, remarks, submittedAt, reviewedAt
     */
    public Map<String, Object> getKycStatus(String customerId) {

        Optional<KycDocument> optionalDoc = kycDocumentRepository.findByCustomerId(customerId);

        Map<String, Object> response = new HashMap<>();

        if (optionalDoc.isEmpty()) {
            // Customer hasn't submitted KYC yet
            response.put("status", "NOT_SUBMITTED");
            response.put("message", "You have not submitted any KYC documents yet. Please submit via POST /api/kyc/submit");
            return response;
        }

        KycDocument doc = optionalDoc.get();

        // Return a clean summary (not the full raw entity with all photo URLs etc.)
        response.put("kycDocumentId",  doc.getId());
        response.put("status",         doc.getKycStatus());
        response.put("remarks",        doc.getRemarks());
        response.put("submittedAt",    doc.getSubmittedAt());
        response.put("reviewedAt",     doc.getReviewedAt());
        response.put("aadhaarNumber",  doc.getAadhaarNumber());
        response.put("panNumber",      doc.getPanNumber());

        return response;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // METHOD 4: getAllPendingKyc
    // Called by: EMPLOYEE via GET /api/kyc/pending
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns all KYC documents that are currently PENDING manual review.
     * Note: In our system, auto-validation runs immediately on submit,
     * so PENDING state only exists for a brief moment or can be forced
     * by the employee if they want to re-review.
     *
     * @return  List of KycDocument objects with status = PENDING
     */
    public List<KycDocument> getAllPendingKyc() {
        return kycDocumentRepository.findByKycStatus(KycStatus.PENDING);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // METHOD 5: getAllKyc
    // Called by: EMPLOYEE via GET /api/kyc/all
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns ALL KYC documents regardless of status.
     * Employee uses this for a full overview.
     *
     * @return  List of all KycDocument objects
     */
    public List<KycDocument> getAllKyc() {
        return kycDocumentRepository.findAll();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // METHOD 6: reviewKyc
    // Called by: EMPLOYEE via PUT /api/kyc/{id}/review
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Allows an EMPLOYEE to manually override the KYC status.
     * They can APPROVE a rejected document or REJECT an approved one.
     * This also updates the customer's kycStatus field.
     *
     * @param kycId   The ID of the KycDocument to review
     * @param dto     Contains the new status and optional remarks
     * @return        The updated KycDocument
     */
    public KycDocument reviewKyc(Long kycId, KycReviewDto dto) {

        // Find the KYC document by ID (throw error if not found)
        KycDocument doc = kycDocumentRepository.findById(kycId)
                .orElseThrow(() -> new RuntimeException("KYC document not found with ID: " + kycId));

        // Update the status from the employee's decision
        doc.setKycStatus(dto.getStatus());

        // Update the remarks (employee's notes)
        doc.setRemarks(dto.getRemarks());

        // Record when the employee reviewed it
        doc.setReviewedAt(LocalDateTime.now());

        // Also update the Customer entity's kycStatus to keep them in sync
        Customer customer = doc.getCustomer();
        customer.setKycStatus(dto.getStatus());
        customerRepository.save(customer);

        // Save and return the updated KYC document
        return kycDocumentRepository.save(doc);
    }
}
