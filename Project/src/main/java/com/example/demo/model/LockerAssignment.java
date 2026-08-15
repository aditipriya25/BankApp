package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "locker_assignment")
public class LockerAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "approved_by_employee_id")
    private String approvedByEmployeeId;

    @Column(name = "locker_id")
    private String lockerId;

    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "request_status")
    private String requestStatus;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    public LockerAssignment() {
    }

    public LockerAssignment(String id, String approvedByEmployeeId, String lockerId, String customerId, String requestStatus, LocalDateTime assignedAt) {
        this.id = id;
        this.approvedByEmployeeId = approvedByEmployeeId;
        this.lockerId = lockerId;
        this.customerId = customerId;
        this.requestStatus = requestStatus;
        this.assignedAt = assignedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApprovedByEmployeeId() {
        return approvedByEmployeeId;
    }

    public void setApprovedByEmployeeId(String approvedByEmployeeId) {
        this.approvedByEmployeeId = approvedByEmployeeId;
    }

    public String getLockerId() {
        return lockerId;
    }

    public void setLockerId(String lockerId) {
        this.lockerId = lockerId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
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