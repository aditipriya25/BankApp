package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.KycDocument;
import com.example.demo.model.KycStatus;

/**
 * KycDocumentRepository
 *
 * Spring Data JPA repository for the KYC_DOCUMENT table.
 * JpaRepository gives us free methods like: save(), findById(), findAll(), delete() etc.
 *
 * We add two custom query methods:
 *  1. findByCustomerId       → find a customer's KYC document by their customer ID
 *  2. findByKycStatus        → find all KYC documents with a given status (e.g., all PENDING)
 */
@Repository
public interface KycDocumentRepository extends JpaRepository<KycDocument, Long> {

    /**
     * Finds the KYC document submitted by a specific customer.
     * Spring Data JPA automatically generates the SQL:
     *   SELECT * FROM KYC_DOCUMENT WHERE customer_id = ?
     *
     * Returns Optional because a customer might not have submitted KYC yet.
     */
    Optional<KycDocument> findByCustomerId(String customerId);

    /**
     * Finds all KYC documents with a specific status.
     * Used by the employee to see all PENDING KYC submissions.
     * Spring Data JPA generates:
     *   SELECT * FROM KYC_DOCUMENT WHERE kyc_status = ?
     */
    List<KycDocument> findByKycStatus(KycStatus kycStatus);
}
