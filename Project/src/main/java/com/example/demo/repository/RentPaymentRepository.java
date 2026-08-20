package com.example.demo.repository;

import com.example.demo.model.RentPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RentPaymentRepository extends JpaRepository<RentPayment, String> {
    List<RentPayment> findByAssignment_IdOrderByPaidAtDesc(String assignmentId);
    Optional<RentPayment> findByAssignment_IdAndPaymentYear(String assignmentId, int year);

    @Query("SELECT rp FROM RentPayment rp WHERE rp.assignment.id = :assignmentId AND rp.status = 'COMPLETED' ORDER BY rp.paymentYear DESC")
    List<RentPayment> findCompletedByAssignmentId(@Param("assignmentId") String assignmentId);

    long countByAssignment_IdAndStatus(String assignmentId, String status);
}
