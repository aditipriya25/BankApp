package com.example.demo.model;

/**
 * KycStatus Enum
 *
 * Represents the 3 possible states of a customer's KYC verification.
 *
 * PENDING  → Customer has submitted documents, waiting for validation result.
 * APPROVED → All dummy checks passed (name match + address match + photo match).
 * REJECTED → One or more checks failed (e.g., address mismatch between Aadhaar and PAN).
 */
public enum KycStatus {
    PENDING,
    APPROVED,
    REJECTED
}
