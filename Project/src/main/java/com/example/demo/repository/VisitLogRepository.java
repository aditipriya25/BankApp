package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.VisitLog;

public interface VisitLogRepository extends JpaRepository<VisitLog, String> {

    Optional<VisitLog> findByBookingId(String bookingId);
}