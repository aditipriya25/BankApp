package com.example.demo.dto;

import java.time.LocalDate;

public class NomineeDto {
    private String name;
    private String relationship;
    private String dateOfBirth; // ISO string yyyy-MM-dd
    private String phone;
    private String email;
    private String address;
    private String photoUrl;
    private boolean isMinor;
    private String guardianName;
    private String guardianRelationship;
    private String guardianPhone;
    private String formType; // SL1, SL1A, SL2, SL3, SL3A

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }
    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
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
}
