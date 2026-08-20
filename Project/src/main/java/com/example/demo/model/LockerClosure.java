package com.example.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Locker Closure entity — RBI Part VI: Closure and Discharge of locker items.
 *
 * Closure types:
 *  - NORMAL:       Customer-initiated (key lost, voluntary surrender) — RBI 6.1
 *  - DEATH:        Death of locker hirer; nominee/survivor claims contents — RBI 5.2, 5.3
 *  - NON_PAYMENT:  Bank-initiated after 3 consecutive years of unpaid rent — RBI 6.3
 *  - INOPERATIVE:  Locker unused for 7 years even with rent paid — RBI 6.4
 *  - LAW_ENFORCEMENT: Court/authority order for attachment — RBI 6.2
 */
@Entity
@Table(name = "locker_closure")
public class LockerClosure {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private LockerAssignment assignment;

    /**
     * Type of closure: NORMAL, DEATH, NON_PAYMENT, INOPERATIVE, LAW_ENFORCEMENT
     */
    @Column(name = "closure_type", nullable = false)
    private String closureType;

    /**
     * Status: REQUESTED, NOTICE_ISSUED, IN_PROGRESS, COMPLETED, CANCELLED
     */
    @Column(name = "status", nullable = false)
    private String status = "REQUESTED";

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    /** RBI 5.2.4: Banks must settle death claims within 15 days */
    @Column(name = "notice_issued_at")
    private LocalDateTime noticeIssuedAt;

    @Column(name = "notice_due_date")
    private LocalDateTime noticeDueDate;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "reason", length = 2000)
    private String reason;

    /** URL to death certificate (for DEATH closure type) */
    @Column(name = "death_certificate_url", length = 2000)
    private String deathCertificateUrl;

    /** Nominee/survivor details string for death closure */
    @Column(name = "claimant_details", length = 2000)
    private String claimantDetails;

    /** Inventory of locker contents as per RBI 6.3.2 and 6.3.3 */
    @Column(name = "inventory_details", columnDefinition = "CLOB")
    private String inventoryDetails;

    /** RBI 6.3.2: Two independent witnesses required for break-open */
    @Column(name = "witness1_name")
    private String witness1Name;

    @Column(name = "witness2_name")
    private String witness2Name;

    /** Employee who processed/approved the closure */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by_employee_id")
    private Employee processedByEmployee;

    /**
     * RBI 7.2: Compensation = 100 × prevailing annual rent
     * (for fire/theft/burglary/employee fraud)
     */
    @Column(name = "compensation_amount", precision = 14, scale = 2)
    private BigDecimal compensationAmount;

    /** URL to video recording of break-open process (RBI 6.2.3, 6.3.2) */
    @Column(name = "video_url", length = 2000)
    private String videoUrl;

    /** Newspaper notice details (RBI 6.3.2: public notice in 2 dailies) */
    @Column(name = "newspaper_notice_details", length = 1000)
    private String newspaperNoticeDetails;

    @Column(name = "court_order_url", length = 2000)
    private String courtOrderUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.requestedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public LockerClosure() {}

    // ─── Getters & Setters ────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LockerAssignment getAssignment() { return assignment; }
    public void setAssignment(LockerAssignment assignment) { this.assignment = assignment; }

    public String getClosureType() { return closureType; }
    public void setClosureType(String closureType) { this.closureType = closureType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

    public LocalDateTime getNoticeIssuedAt() { return noticeIssuedAt; }
    public void setNoticeIssuedAt(LocalDateTime noticeIssuedAt) { this.noticeIssuedAt = noticeIssuedAt; }

    public LocalDateTime getNoticeDueDate() { return noticeDueDate; }
    public void setNoticeDueDate(LocalDateTime noticeDueDate) { this.noticeDueDate = noticeDueDate; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getDeathCertificateUrl() { return deathCertificateUrl; }
    public void setDeathCertificateUrl(String deathCertificateUrl) { this.deathCertificateUrl = deathCertificateUrl; }

    public String getClaimantDetails() { return claimantDetails; }
    public void setClaimantDetails(String claimantDetails) { this.claimantDetails = claimantDetails; }

    public String getInventoryDetails() { return inventoryDetails; }
    public void setInventoryDetails(String inventoryDetails) { this.inventoryDetails = inventoryDetails; }

    public String getWitness1Name() { return witness1Name; }
    public void setWitness1Name(String witness1Name) { this.witness1Name = witness1Name; }

    public String getWitness2Name() { return witness2Name; }
    public void setWitness2Name(String witness2Name) { this.witness2Name = witness2Name; }

    public Employee getProcessedByEmployee() { return processedByEmployee; }
    public void setProcessedByEmployee(Employee processedByEmployee) { this.processedByEmployee = processedByEmployee; }

    public BigDecimal getCompensationAmount() { return compensationAmount; }
    public void setCompensationAmount(BigDecimal compensationAmount) { this.compensationAmount = compensationAmount; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getNewspaperNoticeDetails() { return newspaperNoticeDetails; }
    public void setNewspaperNoticeDetails(String newspaperNoticeDetails) { this.newspaperNoticeDetails = newspaperNoticeDetails; }

    public String getCourtOrderUrl() { return courtOrderUrl; }
    public void setCourtOrderUrl(String courtOrderUrl) { this.courtOrderUrl = courtOrderUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
