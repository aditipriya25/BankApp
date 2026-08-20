package com.example.demo.repository;

import com.example.demo.model.Nominee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NomineeRepository extends JpaRepository<Nominee, String> {
    List<Nominee> findByAssignment_IdAndIsActiveTrue(String assignmentId);
    List<Nominee> findByAssignment_IdOrderByCreatedAtAsc(String assignmentId);
    long countByAssignment_IdAndIsActiveTrue(String assignmentId);
}
