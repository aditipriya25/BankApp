package com.example.demo.dto;

import com.example.demo.model.KycStatus;

/**
 * KycReviewDto — Data Transfer Object for Employee KYC Review
 *
 * This is the "form" that an EMPLOYEE fills in when they manually
 * approve or reject a customer's KYC submission.
 *
 * Fields:
 *   status  → The new KYC status to set: APPROVED or REJECTED
 *             (PENDING is not allowed here — employees always make a final decision)
 *
 *   remarks → Optional notes from the employee explaining the decision.
 *             Example: "Address on Aadhaar does not match PAN address"
 *             Example: "All documents verified successfully"
 *
 * Usage (what the employee sends in the request body):
 * {
 *   "status": "APPROVED",
 *   "remarks": "All documents verified successfully"
 * }
 *
 * or:
 * {
 *   "status": "REJECTED",
 *   "remarks": "Name mismatch between Aadhaar and PAN card"
 * }
 */
public class KycReviewDto {

    private KycStatus status;   // APPROVED or REJECTED
    private String remarks;     // Reason / notes from the employee

    // ─── Constructors ─────────────────────────────────────────────────────────

    public KycReviewDto() {
        super();
    }

    // ─── Getters and Setters ──────────────────────────────────────────────────

    public KycStatus getStatus() {
        return status;
    }

    public void setStatus(KycStatus status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
