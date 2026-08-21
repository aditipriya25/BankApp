package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Notification Entity
 *
 * Stores in-app notifications for both customers and employees.
 * Generated on key events: KYC reviewed, rent paid, closure requested/approved, agreement ready.
 */
@Entity
@Table(name = "NOTIFICATIONS")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Email of the recipient (customer or employee) */
    @Column(nullable = false)
    private String recipientEmail;

    /** CUSTOMER or EMPLOYEE */
    @Column(nullable = false)
    private String recipientRole;

    /** Short title shown in the notification bell */
    @Column(nullable = false)
    private String title;

    /** Full notification message */
    @Column(columnDefinition = "CLOB")
    private String message;

    /**
     * Type of event:
     * KYC_SUBMITTED, KYC_APPROVED, KYC_REJECTED,
     * RENT_PAID, RENT_DUE,
     * CLOSURE_REQUESTED, CLOSURE_APPROVED, CLOSURE_REJECTED,
     * AGREEMENT_READY, AGREEMENT_SIGNED,
     * GENERAL
     */
    @Column(nullable = false)
    private String type;

    /** Whether the recipient has read this notification */
    @Column(nullable = false)
    private boolean isRead = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── Getters & Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }

    public String getRecipientRole() { return recipientRole; }
    public void setRecipientRole(String recipientRole) { this.recipientRole = recipientRole; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
