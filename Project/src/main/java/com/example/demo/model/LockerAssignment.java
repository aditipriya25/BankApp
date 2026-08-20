package com.example.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "locker_assignment")
public class LockerAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "locker_id")
    private Locker locker;

    // @Column(name = "approved_by_employee_id")
    // private String approvedByEmployeeId;
    @ManyToOne
    @JoinColumn(name = "approved_by_employee_id")
    private Employee approvedByEmployee;

    // @Column(name = "locker_id")
    // private String lockerId;

    // @Column(name = "customer_id")
    // private String customerId;

    @Column(name = "request_status")
    private String requestStatus;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "payment_due_date")
    private LocalDateTime paymentDueDate;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    // ─── Rent Tracking Fields (RBI para 2.2, 6.3) ─────────────────────────────

    /** Date up to which rent has been paid */
    @Column(name = "rent_paid_until")
    private LocalDate rentPaidUntil;

    /** Next rent due date */
    @Column(name = "next_rent_due_date")
    private LocalDate nextRentDueDate;

    /** RBI 6.3.1: Bank may break open locker if rent unpaid for 3 consecutive years */
    @Column(name = "consecutive_unpaid_years")
    private int consecutiveUnpaidYears = 0;

    /** Total number of rent payments made */
    @Column(name = "rent_payment_count")
    private int rentPaymentCount = 0;

    // ─── Closure Tracking Fields (RBI Part VI) ────────────────────────────────

    /** NORMAL / DEATH / NON_PAYMENT / INOPERATIVE / LAW_ENFORCEMENT */
    @Column(name = "closure_type")
    private String closureType;

    /** NONE / REQUESTED / IN_PROGRESS / COMPLETED */
    @Column(name = "closure_status")
    private String closureStatus = "NONE";

    @Column(name = "closure_requested_at")
    private LocalDateTime closureRequestedAt;

    @Column(name = "closure_completed_at")
    private LocalDateTime closureCompletedAt;

    /** RBI 6.4.1: Flag for lockers inoperative for 7+ years */
    @Column(name = "last_operated_at")
    private LocalDateTime lastOperatedAt;

    public LockerAssignment() {
    }

    public String getId() {
        return id;
    }

    public LockerAssignment(String id, Customer customer, Locker locker, Employee approvedByEmployee,
            String requestStatus, LocalDateTime assignedAt, String paymentStatus, String paymentMethod,
            LocalDateTime paymentDueDate, LocalDateTime paidAt) {
        this.id = id;
        this.customer = customer;
        this.locker = locker;
        this.approvedByEmployee = approvedByEmployee;
        this.requestStatus = requestStatus;
        this.assignedAt = assignedAt;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
        this.paymentDueDate = paymentDueDate;
        this.paidAt = paidAt;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Locker getLocker() {
        return locker;
    }

    public void setLocker(Locker locker) {
        this.locker = locker;
    }

    public Employee getApprovedByEmployee() {
        return approvedByEmployee;
    }

    public void setApprovedByEmployee(Employee approvedByEmployee) {
        this.approvedByEmployee = approvedByEmployee;
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getPaymentDueDate() {
        return paymentDueDate;
    }

    public void setPaymentDueDate(LocalDateTime paymentDueDate) {
        this.paymentDueDate = paymentDueDate;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public java.time.LocalDate getRentPaidUntil() { return rentPaidUntil; }
    public void setRentPaidUntil(java.time.LocalDate rentPaidUntil) { this.rentPaidUntil = rentPaidUntil; }

    public java.time.LocalDate getNextRentDueDate() { return nextRentDueDate; }
    public void setNextRentDueDate(java.time.LocalDate nextRentDueDate) { this.nextRentDueDate = nextRentDueDate; }

    public int getConsecutiveUnpaidYears() { return consecutiveUnpaidYears; }
    public void setConsecutiveUnpaidYears(int consecutiveUnpaidYears) { this.consecutiveUnpaidYears = consecutiveUnpaidYears; }

    public int getRentPaymentCount() { return rentPaymentCount; }
    public void setRentPaymentCount(int rentPaymentCount) { this.rentPaymentCount = rentPaymentCount; }

    public String getClosureType() { return closureType; }
    public void setClosureType(String closureType) { this.closureType = closureType; }

    public String getClosureStatus() { return closureStatus; }
    public void setClosureStatus(String closureStatus) { this.closureStatus = closureStatus; }

    public LocalDateTime getClosureRequestedAt() { return closureRequestedAt; }
    public void setClosureRequestedAt(LocalDateTime closureRequestedAt) { this.closureRequestedAt = closureRequestedAt; }

    public LocalDateTime getClosureCompletedAt() { return closureCompletedAt; }
    public void setClosureCompletedAt(LocalDateTime closureCompletedAt) { this.closureCompletedAt = closureCompletedAt; }

    public LocalDateTime getLastOperatedAt() { return lastOperatedAt; }
    public void setLastOperatedAt(LocalDateTime lastOperatedAt) { this.lastOperatedAt = lastOperatedAt; }
}