package com.example.demo.repository;

import com.example.demo.model.LockerAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LockerAgreementRepository extends JpaRepository<LockerAgreement, String> {
    Optional<LockerAgreement> findByAssignment_Id(String assignmentId);
    long countBySignedByCustomerFalse();
}
