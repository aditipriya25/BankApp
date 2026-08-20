package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Nominee for a safe deposit locker assignment.
 * As per RBI circular DOR.LEG.REC/40/09.07.005/2021-22, Section 5.1:
 * Banks shall offer nomination facility in case of safe deposit lockers per
 * Banking Regulation Act 1949, Sections 45ZC to 45ZF.
 * Forms: SL1 (nomination), SL1A (joint), SL2 (cancellation), SL3 (variation).
 */
@Entity
@Table(name = "locker_nominee")
public class Nominee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private LockerAssignment assignment;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "relationship", nullable = false)
    private String relationship;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "address", length = 1000)
    private String address;

    /** Passport-size photo URL as required by RBI guidelines para 5.1.1 */
    @Column(name = "photo_url", length = 2000)
    private String photoUrl;

    /** If nominee is a minor, minor guardian's details are required (RBI 5.1.1) */
    @Column(name = "is_minor")
    private boolean isMinor = false;

    @Column(name = "guardian_name")
    private String guardianName;

    @Column(name = "guardian_relationship")
    private String guardianRelationship;

    @Column(name = "guardian_phone")
    private String guardianPhone;

    /** Nomination form type as per Banking Companies (Nomination) Rules 1985 */
    @Column(name = "form_type")
    private String formType; // SL1, SL1A, SL2, SL3, SL3A

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_active")
    private boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Nominee() {}

    // ─── Getters & Setters ────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LockerAssignment getAssignment() { return assignment; }
    public void setAssignment(LockerAssignment assignment) { this.assignment = assignment; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public boolean isMinor() { return isMinor; }
    public void setMinor(boolean minor) { isMinor = minor; }

    public String getGuardianName() { return guardianName; }
    public void setGuardianName(String guardianName) { this.guardianName = guardianName; }

    public String getGuardianRelationship() { return guardianRelationship; }
    public void setGuardianRelationship(String guardianRelationship) { this.guardianRelationship = guardianRelationship; }

    public String getGuardianPhone() { return guardianPhone; }
    public void setGuardianPhone(String guardianPhone) { this.guardianPhone = guardianPhone; }

    public String getFormType() { return formType; }
    public void setFormType(String formType) { this.formType = formType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
