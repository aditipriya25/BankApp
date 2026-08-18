package com.example.demo.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * KycDocument Entity
 *
 * This class maps to the "KYC_DOCUMENT" table in Oracle DB.
 * It stores all the information a customer submits for KYC verification:
 *   - Aadhaar card details (number, name, address, photo URL)
 *   - PAN card details (number, name, address)
 *   - Live/selfie photo URL
 *   - A dummy photoMatchFlag (true = photo matches, false = doesn't match)
 *   - The KYC status (PENDING / APPROVED / REJECTED)
 *   - Timestamps and remarks
 *
 * Linked to: Customer (OneToOne — one customer has one KYC document)
 */
@Entity
@Table(name = "KYC_DOCUMENT")
public class KycDocument {

    // ─── Primary Key ──────────────────────────────────────────────────────────
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    // ─── Relationship: each KYC document belongs to exactly one Customer ──────
    @OneToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    @JsonIgnore  // Prevents infinite loop when serialising Customer → KycDocument → Customer
    private Customer customer;

    // ─── Aadhaar Card Details ─────────────────────────────────────────────────

    @Column(name = "aadhaar_number", length = 12)
    private String aadhaarNumber;    // Dummy 12-digit number, e.g. "123456789012"

    @Column(name = "aadhaar_name")
    private String aadhaarName;      // Name printed on the Aadhaar card

    @Column(name = "aadhaar_address", length = 500)
    private String aadhaarAddress;   // Address printed on the Aadhaar card

    @Column(name = "aadhaar_photo_url", length = 500)
    private String aadhaarPhotoUrl;  // Dummy URL pointing to the Aadhaar photo, e.g. "https://dummy.photos/aadhaar_aditi.jpg"

    // ─── PAN Card Details ─────────────────────────────────────────────────────

    @Column(name = "pan_number", length = 10)
    private String panNumber;        // Dummy 10-character PAN, e.g. "ABCDE1234F"

    @Column(name = "pan_name")
    private String panName;          // Name printed on the PAN card

    @Column(name = "pan_address", length = 500)
    private String panAddress;       // Address printed on the PAN card

    // ─── Live / Selfie Photo ──────────────────────────────────────────────────

    @Column(name = "live_photo_url", length = 500)
    private String livePhotoUrl;     // Dummy URL of the customer's live selfie photo

    /**
     * photoMatchFlag — Dummy face-match simulation.
     *
     * In a real bank, a face-recognition AI would compare the Aadhaar photo
     * with the live selfie photo. Here, we ask the customer to set this flag:
     *   true  → "My live photo matches my Aadhaar photo" → treated as MATCH
     *   false → "Photos don't match"                     → treated as MISMATCH
     */
    @Column(name = "photo_match_flag")
    private boolean photoMatchFlag;

    // ─── KYC Status ───────────────────────────────────────────────────────────

    /**
     * The current status of this KYC document.
     * Stored in DB as a String ("PENDING", "APPROVED", "REJECTED") using EnumType.STRING.
     * Default is PENDING when the customer first submits.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status")
    private KycStatus kycStatus = KycStatus.PENDING;

    // ─── Timestamps ───────────────────────────────────────────────────────────

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;  // When the customer submitted the KYC

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;   // When the employee reviewed it (null if not yet reviewed)

    // ─── Remarks ──────────────────────────────────────────────────────────────

    @Column(name = "remarks", length = 1000)
    private String remarks;   // Optional notes, e.g. "Address mismatch between Aadhaar and PAN"

    // ─── Constructors ─────────────────────────────────────────────────────────

    public KycDocument() {
        super();
    }

    // ─── Getters and Setters ──────────────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getAadhaarNumber() {
        return aadhaarNumber;
    }

    public void setAadhaarNumber(String aadhaarNumber) {
        this.aadhaarNumber = aadhaarNumber;
    }

    public String getAadhaarName() {
        return aadhaarName;
    }

    public void setAadhaarName(String aadhaarName) {
        this.aadhaarName = aadhaarName;
    }

    public String getAadhaarAddress() {
        return aadhaarAddress;
    }

    public void setAadhaarAddress(String aadhaarAddress) {
        this.aadhaarAddress = aadhaarAddress;
    }

    public String getAadhaarPhotoUrl() {
        return aadhaarPhotoUrl;
    }

    public void setAadhaarPhotoUrl(String aadhaarPhotoUrl) {
        this.aadhaarPhotoUrl = aadhaarPhotoUrl;
    }

    public String getPanNumber() {
        return panNumber;
    }

    public void setPanNumber(String panNumber) {
        this.panNumber = panNumber;
    }

    public String getPanName() {
        return panName;
    }

    public void setPanName(String panName) {
        this.panName = panName;
    }

    public String getPanAddress() {
        return panAddress;
    }

    public void setPanAddress(String panAddress) {
        this.panAddress = panAddress;
    }

    public String getLivePhotoUrl() {
        return livePhotoUrl;
    }

    public void setLivePhotoUrl(String livePhotoUrl) {
        this.livePhotoUrl = livePhotoUrl;
    }

    public boolean isPhotoMatchFlag() {
        return photoMatchFlag;
    }

    public void setPhotoMatchFlag(boolean photoMatchFlag) {
        this.photoMatchFlag = photoMatchFlag;
    }

    public KycStatus getKycStatus() {
        return kycStatus;
    }

    public void setKycStatus(KycStatus kycStatus) {
        this.kycStatus = kycStatus;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public String toString() {
        return "KycDocument [id=" + id + ", aadhaarNumber=" + aadhaarNumber
                + ", panNumber=" + panNumber + ", kycStatus=" + kycStatus
                + ", submittedAt=" + submittedAt + "]";
    }
}
