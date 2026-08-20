package com.example.demo.dto;

public class LockerClosureDto {
    /** NORMAL, DEATH, NON_PAYMENT, INOPERATIVE, LAW_ENFORCEMENT */
    private String closureType;
    private String reason;
    private String deathCertificateUrl;
    private String claimantDetails;   // nominee/survivor details
    private String courtOrderUrl;     // for law enforcement closure
    private String inventoryDetails;
    private String witness1Name;
    private String witness2Name;
    private String videoUrl;
    private String newspaperNoticeDetails;

    public String getClosureType() { return closureType; }
    public void setClosureType(String closureType) { this.closureType = closureType; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getDeathCertificateUrl() { return deathCertificateUrl; }
    public void setDeathCertificateUrl(String deathCertificateUrl) { this.deathCertificateUrl = deathCertificateUrl; }
    public String getClaimantDetails() { return claimantDetails; }
    public void setClaimantDetails(String claimantDetails) { this.claimantDetails = claimantDetails; }
    public String getCourtOrderUrl() { return courtOrderUrl; }
    public void setCourtOrderUrl(String courtOrderUrl) { this.courtOrderUrl = courtOrderUrl; }
    public String getInventoryDetails() { return inventoryDetails; }
    public void setInventoryDetails(String inventoryDetails) { this.inventoryDetails = inventoryDetails; }
    public String getWitness1Name() { return witness1Name; }
    public void setWitness1Name(String witness1Name) { this.witness1Name = witness1Name; }
    public String getWitness2Name() { return witness2Name; }
    public void setWitness2Name(String witness2Name) { this.witness2Name = witness2Name; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public String getNewspaperNoticeDetails() { return newspaperNoticeDetails; }
    public void setNewspaperNoticeDetails(String newspaperNoticeDetails) { this.newspaperNoticeDetails = newspaperNoticeDetails; }
}
