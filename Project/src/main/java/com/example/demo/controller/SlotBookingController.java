package com.example.demo.controller;

import com.example.demo.model.LockerAssignment;
import com.example.demo.model.SlotBooking;
import com.example.demo.repository.LockerAssignmentRepository;
import com.example.demo.service.SlotBookingService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/slot-bookings")
public class SlotBookingController {

    private final SlotBookingService slotBookingService;
    private final LockerAssignmentRepository lockerAssignmentRepository;

    // Statuses that allow a customer to book a visit slot
    private static final List<String> BOOKABLE_STATUSES = Arrays.asList("APPROVED", "PAID");

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
                .findByCustomer_EmailAndRequestStatusIn(email, BOOKABLE_STATUSES)
                .orElseThrow(() -> new RuntimeException(
                        "Customer does not have an approved or paid locker assignment"));

        SlotBooking booking = slotBookingService.bookSlot(assignment.getCustomer().getId(),
                LocalDateTime.parse(scheduledAt));

        return ResponseEntity.ok(booking);
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<List<SlotBooking>> getMyBookings(
            Authentication authentication) {
        String email = authentication.getName();

        LockerAssignment assignment = lockerAssignmentRepository
                .findByCustomer_EmailAndRequestStatusIn(email, BOOKABLE_STATUSES)
                .orElseThrow(() -> new RuntimeException(
                        "Customer does not have an approved or paid locker assignment"));

        return ResponseEntity.ok(slotBookingService.getBookingsForAssignment(assignment.getId()));
    }
}