package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.LockerAssignment;

public interface LockerAssignmentRepository extends JpaRepository<LockerAssignment, String> {
    List<LockerAssignment> findByRequestStatus(String requestStatus);

    Optional<LockerAssignment> findByCustomerIdAndRequestStatus(
            String customerId,
            String requestStatus);

    Optional<LockerAssignment> findByCustomer_EmailAndRequestStatus(
            String email,
            String requestStatus);

    Optional<LockerAssignment> findByCustomerIdAndRequestStatusIn(
            String customerId,
            List<String> requestStatuses);

    List<LockerAssignment> findByCustomer_Email(String email);

    Optional<LockerAssignment> findByCustomer_EmailAndRequestStatusIn(
            String email,
            List<String> requestStatuses);
}