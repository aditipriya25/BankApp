package com.example.demo.dto;

/**
 * KycRequestDto — Data Transfer Object for KYC Submission
 *
 * This is the "form" that a CUSTOMER fills in and sends to the server
 * when they want to submit their KYC documents.
 *
 * DTO (Data Transfer Object) means this class is only used to carry data
 * between the HTTP request body and the service layer.
 * It is NOT saved to the DB directly — we map it to a KycDocument entity first.
 *
 * ─── AADHAAR CARD SECTION ────────────────────────────────────────────────────
 * aadhaarNumber  → 12-digit Aadhaar number    e.g. "123456789012"
 * aadhaarName    → Name on Aadhaar            e.g. "Aditi Priya"
 * aadhaarAddress → Address on Aadhaar         e.g. "123, MG Road, Bangalore"
 * aadhaarPhotoUrl→ URL of Aadhaar card photo  e.g. "https://dummy.photos/aadhaar.jpg"
 *
 * ─── PAN CARD SECTION ────────────────────────────────────────────────────────
 * panNumber      → 10-character PAN number    e.g. "ABCDE1234F"
 * panName        → Name on PAN card           e.g. "Aditi Priya"
 * panAddress     → Address on PAN card        e.g. "123, MG Road, Bangalore"
 *
 * ─── LIVE PHOTO SECTION ──────────────────────────────────────────────────────
 * livePhotoUrl   → URL of selfie/live photo   e.g. "https://dummy.photos/selfie.jpg"
 *
 * ─── DUMMY PHOTO MATCH FLAG ──────────────────────────────────────────────────
 * photoMatchFlag → true  = "My live photo looks like my Aadhaar photo" (PASS)
 *                  false = "Photos don't match"                         (FAIL)
 *
 * In a real bank: an AI would compare the photos automatically.
 * In this dummy project: the customer simply tells us if they match.
 */
public class KycRequestDto {

    // ─── Aadhaar Card ─────────────────────────────────────────────────────────
    private String aadhaarNumber;
    private String aadhaarName;
    private String aadhaarAddress;
    private String aadhaarPhotoUrl;

    // ─── PAN Card ─────────────────────────────────────────────────────────────
    private String panNumber;
    private String panName;
    private String panAddress;

    // ─── Live Photo ───────────────────────────────────────────────────────────
    private String livePhotoUrl;

    // ─── Dummy Photo Match ────────────────────────────────────────────────────
    private boolean photoMatchFlag;

    // ─── Constructors ─────────────────────────────────────────────────────────

    public KycRequestDto() {
        super();
    }

    // ─── Getters and Setters ──────────────────────────────────────────────────

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
}
