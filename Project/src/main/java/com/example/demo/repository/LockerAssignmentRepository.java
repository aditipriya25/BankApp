package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.LockerAssignment;

public interface LockerAssignmentRepository extends JpaRepository<LockerAssignment, String> {
    List<LockerAssignment> findByRequestStatus(String requestStatus);
}