package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Locker Agreement entity — RBI para 2.1:
 * "Banks shall have a Board approved agreement for safe deposit lockers...
 *  Banks shall renew their locker agreements with existing customers by January 1, 2023."
 * Agreement must be on stamped paper, signed by both parties, copy given to customer.
 */
@Entity
@Table(name = "locker_agreement")
public class LockerAgreement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", unique = true)
    private LockerAssignment assignment;

    @Column(name = "agreement_date")
    private LocalDate agreementDate;

    /** Full text of the agreement as per IBA model agreement template */
    @Column(name = "agreement_content", columnDefinition = "CLOB")
    private String agreementContent;

    @Column(name = "terms_accepted")
    private boolean termsAccepted = false;

    /** RBI 2.1.2: Agreement must be on stamped paper */
    @Column(name = "stamp_duty_paid")
    private boolean stampDutyPaid = false;

    @Column(name = "stamp_duty_amount")
    private Double stampDutyAmount;

    /** URL to the signed PDF agreement (bank copy retained per RBI 2.1.2) */
    @Column(name = "agreement_pdf_url", length = 2000)
    private String agreementPdfUrl;

    /** Customer digital acceptance */
    @Column(name = "signed_by_customer")
    private boolean signedByCustomer = false;

    @Column(name = "customer_signed_at")
    private LocalDateTime customerSignedAt;

    /** Employee who created/countersigned */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signed_by_employee_id")
    private Employee signedByEmployee;

    @Column(name = "employee_signed_at")
    private LocalDateTime employeeSignedAt;

    /** RBI 2.1.1: Banks must renew agreements. Renewal due by Jan 1, 2023 */
    @Column(name = "renewal_due")
    private LocalDate renewalDue;

    @Column(name = "is_renewed")
    private boolean isRenewed = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public LockerAgreement() {}

    // ─── Getters & Setters ────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LockerAssignment getAssignment() { return assignment; }
    public void setAssignment(LockerAssignment assignment) { this.assignment = assignment; }

    public LocalDate getAgreementDate() { return agreementDate; }
    public void setAgreementDate(LocalDate agreementDate) { this.agreementDate = agreementDate; }

    public String getAgreementContent() { return agreementContent; }
    public void setAgreementContent(String agreementContent) { this.agreementContent = agreementContent; }

    public boolean isTermsAccepted() { return termsAccepted; }
    public void setTermsAccepted(boolean termsAccepted) { this.termsAccepted = termsAccepted; }

    public boolean isStampDutyPaid() { return stampDutyPaid; }
    public void setStampDutyPaid(boolean stampDutyPaid) { this.stampDutyPaid = stampDutyPaid; }

    public Double getStampDutyAmount() { return stampDutyAmount; }
    public void setStampDutyAmount(Double stampDutyAmount) { this.stampDutyAmount = stampDutyAmount; }

    public String getAgreementPdfUrl() { return agreementPdfUrl; }
    public void setAgreementPdfUrl(String agreementPdfUrl) { this.agreementPdfUrl = agreementPdfUrl; }

    public boolean isSignedByCustomer() { return signedByCustomer; }
    public void setSignedByCustomer(boolean signedByCustomer) { this.signedByCustomer = signedByCustomer; }

    public LocalDateTime getCustomerSignedAt() { return customerSignedAt; }
    public void setCustomerSignedAt(LocalDateTime customerSignedAt) { this.customerSignedAt = customerSignedAt; }

    public Employee getSignedByEmployee() { return signedByEmployee; }
    public void setSignedByEmployee(Employee signedByEmployee) { this.signedByEmployee = signedByEmployee; }

    public LocalDateTime getEmployeeSignedAt() { return employeeSignedAt; }
    public void setEmployeeSignedAt(LocalDateTime employeeSignedAt) { this.employeeSignedAt = employeeSignedAt; }

    public LocalDate getRenewalDue() { return renewalDue; }
    public void setRenewalDue(LocalDate renewalDue) { this.renewalDue = renewalDue; }

    public boolean isRenewed() { return isRenewed; }
    public void setRenewed(boolean renewed) { isRenewed = renewed; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
