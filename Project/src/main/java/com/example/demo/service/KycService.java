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
 * Submit → always PENDING (employee must review)
 * Employee reviews → APPROVED or REJECTED
 * Notifications sent to customer on review, and to employee on new submission.
 */
@Service
public class KycService {

    @Autowired
    private KycDocumentRepository kycDocumentRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private NotificationService notificationService;

    // ─────────────────────────────────────────────────────────────────────────
    // METHOD 1: submitKyc
    // Called by: CUSTOMER via POST /api/kyc/submit
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Accepts KYC submission from a customer, sets status to PENDING (employee reviews).
     * Notifies all employees that a new KYC is awaiting review.
     */
    public KycDocument submitKyc(String customerId, KycRequestDto dto) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));

        KycDocument kycDocument = kycDocumentRepository
                .findByCustomerId(customerId)
                .orElse(new KycDocument());

        kycDocument.setCustomer(customer);
        kycDocument.setAadhaarNumber(dto.getAadhaarNumber());
        kycDocument.setAadhaarName(dto.getAadhaarName());
        kycDocument.setAadhaarAddress(dto.getAadhaarAddress());
        kycDocument.setAadhaarPhotoUrl(dto.getAadhaarPhotoUrl());
        kycDocument.setPanNumber(dto.getPanNumber());
        kycDocument.setPanName(dto.getPanName());
        kycDocument.setPanAddress(dto.getPanAddress());
        kycDocument.setLivePhotoUrl(dto.getLivePhotoUrl());
        // photoMatchFlag is NOT set by customer — removed from customer workflow
        kycDocument.setPhotoMatchFlag(false);
        kycDocument.setSubmittedAt(LocalDateTime.now());
        kycDocument.setReviewedAt(null);

        // Always set PENDING — employee must review
        kycDocument.setKycStatus(KycStatus.PENDING);
        kycDocument.setRemarks("Awaiting employee verification.");

        // Update customer KYC status
        customer.setKycStatus(KycStatus.PENDING);
        customerRepository.save(customer);

        KycDocument saved = kycDocumentRepository.save(kycDocument);

        // Notify the customer that submission was received
        notificationService.createNotification(
                customer.getEmail(),
                "CUSTOMER",
                "KYC Submitted",
                "Your KYC documents have been submitted and are awaiting employee verification. You will be notified once reviewed.",
                "KYC_SUBMITTED"
        );

        return saved;
    }

    /**
     * Same as submitKyc but resolves the customer from their email (JWT token).
     */
    public KycDocument submitKycByEmail(String email, KycRequestDto dto) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found with email: " + email));
        return submitKyc(customer.getId(), dto);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // METHOD 2: getKycStatus
    // ─────────────────────────────────────────────────────────────────────────

    public Map<String, Object> getKycStatus(String customerId) {
        Optional<KycDocument> optionalDoc = kycDocumentRepository.findByCustomerId(customerId);
        Map<String, Object> response = new HashMap<>();

        if (optionalDoc.isEmpty()) {
            response.put("status", "NOT_SUBMITTED");
            response.put("message", "You have not submitted any KYC documents yet.");
            return response;
        }

        KycDocument doc = optionalDoc.get();
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
    // METHOD 3: getAllPendingKyc  (EMPLOYEE)
    // ─────────────────────────────────────────────────────────────────────────

    public List<KycDocument> getAllPendingKyc() {
        return kycDocumentRepository.findByKycStatus(KycStatus.PENDING);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // METHOD 4: getAllKyc  (EMPLOYEE)
    // ─────────────────────────────────────────────────────────────────────────

    public List<KycDocument> getAllKyc() {
        return kycDocumentRepository.findAll();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // METHOD 5: reviewKyc  (EMPLOYEE)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Employee manually approves or rejects a KYC document.
     * Sends a notification to the customer with the decision.
     */
    public KycDocument reviewKyc(Long kycId, KycReviewDto dto) {

        KycDocument doc = kycDocumentRepository.findById(kycId)
                .orElseThrow(() -> new RuntimeException("KYC document not found with ID: " + kycId));

        // Employee sets the photo match flag during review
        doc.setPhotoMatchFlag(dto.getStatus() == KycStatus.APPROVED);
        doc.setKycStatus(dto.getStatus());
        doc.setRemarks(dto.getRemarks());
        doc.setReviewedAt(LocalDateTime.now());

        Customer customer = doc.getCustomer();
        customer.setKycStatus(dto.getStatus());
        customerRepository.save(customer);

        KycDocument updated = kycDocumentRepository.save(doc);

        // Notify the customer of the decision
        boolean approved = dto.getStatus() == KycStatus.APPROVED;
        notificationService.createNotification(
                customer.getEmail(),
                "CUSTOMER",
                approved ? "KYC Approved ✓" : "KYC Rejected",
                approved
                    ? "Your KYC has been approved. You can now request a locker."
                    : "Your KYC was rejected. Reason: " + dto.getRemarks() + ". Please re-submit with correct documents.",
                approved ? "KYC_APPROVED" : "KYC_REJECTED"
        );

        return updated;
    }
}
