package com.example.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Rent payment record for annual locker rent.
 * RBI para 2.2: Banks are allowed to obtain a Term Deposit covering three years' rent.
 * Banks must refund proportionate advance rent on surrender (2.2.2).
 * Non-payment for 3 years triggers forced closure (6.3.1).
 */
@Entity
@Table(name = "locker_rent_payment")
public class RentPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private LockerAssignment assignment;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /** Payment method: UPI, CARD, NETBANKING, OFFLINE */
    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    /** Transaction / reference ID from gateway or offline receipt */
    @Column(name = "transaction_id", unique = true)
    private String transactionId;

    /** Receipt number for official rent payment document */
    @Column(name = "receipt_number")
    private String receiptNumber;

    /** The year this rent covers (e.g., 2024 means Jan-Dec 2024) */
    @Column(name = "payment_year")
    private Integer paymentYear;

    /** Period start date of this rent payment */
    @Column(name = "period_start")
    private LocalDate periodStart;

    /** Period end date of this rent payment */
    @Column(name = "period_end")
    private LocalDate periodEnd;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    /** Status: PENDING, COMPLETED, FAILED, REFUNDED */
    @Column(name = "status")
    private String status = "PENDING";

    /** Gateway response code / message */
    @Column(name = "gateway_response", length = 500)
    private String gatewayResponse;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public RentPayment() {}

    // ─── Getters & Setters ────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LockerAssignment getAssignment() { return assignment; }
    public void setAssignment(LockerAssignment assignment) { this.assignment = assignment; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }

    public Integer getPaymentYear() { return paymentYear; }
    public void setPaymentYear(Integer paymentYear) { this.paymentYear = paymentYear; }

    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }

    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getGatewayResponse() { return gatewayResponse; }
    public void setGatewayResponse(String gatewayResponse) { this.gatewayResponse = gatewayResponse; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
