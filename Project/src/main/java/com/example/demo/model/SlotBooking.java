package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "slot_booking")
public class SlotBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "assignment_id")
    private String assignmentId;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "otp_code")
    private String otpCode;

    private String status;

    public SlotBooking() {
    }

    public SlotBooking(String id, String assignmentId, LocalDateTime scheduledAt, String otpCode, String status) {
        this.id = id;
        this.assignmentId = assignmentId;
        this.scheduledAt = scheduledAt;
        this.otpCode = otpCode;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(String assignmentId) {
        this.assignmentId = assignmentId;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}