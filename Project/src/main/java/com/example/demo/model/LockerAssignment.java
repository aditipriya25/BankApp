package com.example.demo.model;

import jakarta.persistence.*;
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

    public LockerAssignment() {
    }

    public String getId() {
        return id;
    }

    public LockerAssignment(String id, Customer customer, Locker locker, Employee approvedByEmployee,
            String requestStatus, LocalDateTime assignedAt) {
        this.id = id;
        this.customer = customer;
        this.locker = locker;
        this.approvedByEmployee = approvedByEmployee;
        this.requestStatus = requestStatus;
        this.assignedAt = assignedAt;
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
}