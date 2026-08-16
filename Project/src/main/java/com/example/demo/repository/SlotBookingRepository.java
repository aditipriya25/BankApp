package com.example.demo.repository;

import com.example.demo.model.SlotBooking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SlotBookingRepository extends JpaRepository<SlotBooking, String> {

    List<SlotBooking> findByAssignmentId(String assignmentId);
}
