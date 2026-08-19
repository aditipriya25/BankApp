package com.example.demo.service;

import com.example.demo.model.Customer;
import com.example.demo.model.LockerAssignment;
import com.example.demo.model.SlotBooking;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.LockerAssignmentRepository;
import com.example.demo.repository.SlotBookingRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
public class SlotBookingService {

    private static final List<String> BOOKABLE_STATUSES = Arrays.asList("APPROVED", "PAID");

    private final CustomerRepository customerRepository;
    private final SlotBookingRepository slotBookingRepository;
    private final LockerAssignmentRepository lockerAssignmentRepository;

    public SlotBookingService(
            SlotBookingRepository slotBookingRepository,
            LockerAssignmentRepository lockerAssignmentRepository,
            CustomerRepository customerRepository) {

        this.slotBookingRepository = slotBookingRepository;
        this.lockerAssignmentRepository = lockerAssignmentRepository;
        this.customerRepository = customerRepository;
    }

    public SlotBooking bookSlot(String customerId, LocalDateTime scheduledAt) {

        LockerAssignment assignment = lockerAssignmentRepository
                .findByCustomerIdAndRequestStatusIn(customerId, BOOKABLE_STATUSES)
                .orElseThrow(() -> new RuntimeException(
                        "Customer does not have an approved or paid locker assignment"));

        String otp = generateOtp();

        SlotBooking booking = new SlotBooking();

        booking.setAssignmentId(assignment.getId());
        booking.setScheduledAt(scheduledAt);
        booking.setOtpCode(otp);
        booking.setStatus("BOOKED");

        return slotBookingRepository.save(booking);
    }

    public List<SlotBooking> getBookingsForAssignment(String assignmentId) {
        return slotBookingRepository.findByAssignmentId(assignmentId);
    }

    private String generateOtp() {
        Random random = new Random();
        return String.format(
                "%06d",
                random.nextInt(1000000));
    }

    public List<SlotBooking> getMyBookings(String customerEmail) {

        Customer customer = customerRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        LockerAssignment assignment = lockerAssignmentRepository
                .findByCustomerIdAndRequestStatusIn(
                        customer.getId(),
                        BOOKABLE_STATUSES)
                .orElseThrow(() -> new RuntimeException(
                        "No approved or paid locker assignment found"));

        return slotBookingRepository.findByAssignmentId(assignment.getId());
    }
}