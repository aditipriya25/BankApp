package com.example.demo.controller;

import com.example.demo.model.LockerAssignment;
import com.example.demo.model.SlotBooking;
import com.example.demo.repository.LockerAssignmentRepository;
import com.example.demo.service.SlotBookingService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/slot-bookings")
public class SlotBookingController {

    private final SlotBookingService slotBookingService;
    private final LockerAssignmentRepository lockerAssignmentRepository;

    public SlotBookingController(SlotBookingService slotBookingService,
            LockerAssignmentRepository lockerAssignmentRepository) {
        this.slotBookingService = slotBookingService;
        this.lockerAssignmentRepository = lockerAssignmentRepository;
    }

    @PostMapping("/book")
    public ResponseEntity<SlotBooking> bookSlot(@RequestParam String scheduledAt,
            Authentication authentication) {

        String email = authentication.getName();
        LockerAssignment assignment = lockerAssignmentRepository
                .findByCustomer_EmailAndRequestStatus(email, "APPROVED")
                .orElseThrow(() -> new RuntimeException("Customer does not have an approved locker"));

        SlotBooking booking = slotBookingService.bookSlot(assignment.getCustomer().getId(),
                LocalDateTime.parse(scheduledAt));

        return ResponseEntity.ok(booking);
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<List<SlotBooking>> getMyBookings(
            Authentication authentication) {
        String email = authentication.getName();

        LockerAssignment assignment = lockerAssignmentRepository
                .findByCustomer_EmailAndRequestStatus(email, "APPROVED")
                .orElseThrow(() -> new RuntimeException("Customer does not have an approved locker"));

        return ResponseEntity.ok(slotBookingService.getBookingsForAssignment(assignment.getId()));
    }
}