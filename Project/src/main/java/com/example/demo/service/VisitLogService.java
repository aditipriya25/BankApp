package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.model.LockerAssignment;
import com.example.demo.model.SlotBooking;
import com.example.demo.model.VisitLog;
import com.example.demo.repository.LockerAssignmentRepository;
import com.example.demo.repository.SlotBookingRepository;
import com.example.demo.repository.VisitLogRepository;

@Service
public class VisitLogService {

    private final VisitLogRepository visitLogRepository;
    private final SlotBookingRepository slotBookingRepository;
    private final LockerAssignmentRepository lockerAssignmentRepository;

    public VisitLogService(
            VisitLogRepository visitLogRepository,
            SlotBookingRepository slotBookingRepository,
            LockerAssignmentRepository lockerAssignmentRepository) {

        this.visitLogRepository = visitLogRepository;
        this.slotBookingRepository = slotBookingRepository;
        this.lockerAssignmentRepository = lockerAssignmentRepository;
    }

    public VisitLog verifyOtp(
            String bookingId,
            String otpCode,
            String employeeId) {

        SlotBooking booking = slotBookingRepository
                .findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getOtpCode().equals(otpCode)) {
            throw new RuntimeException("Invalid OTP");
        }

        if (!"BOOKED".equals(booking.getStatus())) {
            throw new RuntimeException("Booking is not active");
        }

        if (visitLogRepository.findByBookingId(bookingId).isPresent()) {
            throw new RuntimeException("OTP already verified");
        }

        VisitLog visitLog = new VisitLog();

        visitLog.setBookingId(bookingId);
        visitLog.setLoggedByEmployeeId(employeeId);
        visitLog.setKeyIssuedAt(LocalDateTime.now());

        booking.setStatus("VISITED");
        slotBookingRepository.save(booking);

        return visitLogRepository.save(visitLog);
    }

    public VisitLog returnKey(String visitLogId) {

        VisitLog visitLog = visitLogRepository
                .findById(visitLogId)
                .orElseThrow(() -> new RuntimeException("Visit log not found"));

        if (visitLog.getKeyReturnedAt() != null) {
            throw new RuntimeException("Key already returned");
        }

        visitLog.setKeyReturnedAt(LocalDateTime.now());

        return visitLogRepository.save(visitLog);
    }

    public List<VisitLog> getAllVisitLogs() {
        return visitLogRepository.findAll();
    }
}