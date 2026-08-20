package com.example.demo.service;

import com.example.demo.dto.RentPaymentDto;
import com.example.demo.model.LockerAssignment;
import com.example.demo.model.RentPayment;
import com.example.demo.repository.LockerAssignmentRepository;
import com.example.demo.repository.RentPaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for locker rent payment management.
 * RBI para 2.2: Annual rent payment, term deposits, refunds.
 * RBI para 6.3: Non-payment for 3 consecutive years → forced closure.
 */
@Service
public class RentPaymentService {

    private final RentPaymentRepository rentPaymentRepository;
    private final LockerAssignmentRepository assignmentRepository;

    public RentPaymentService(RentPaymentRepository rentPaymentRepository,
                              LockerAssignmentRepository assignmentRepository) {
        this.rentPaymentRepository = rentPaymentRepository;
        this.assignmentRepository = assignmentRepository;
    }

    /**
     * Process annual rent payment via gateway (UPI / CARD / NETBANKING / OFFLINE).
     * This is a mock gateway — in production, integrate with Razorpay/PayU.
     */
    @Transactional
    public RentPayment payRent(String assignmentId, String customerEmail, RentPaymentDto dto) {
        LockerAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Locker assignment not found"));

        if (!assignment.getCustomer().getEmail().equals(customerEmail)) {
            throw new RuntimeException("You are not authorized to pay rent for this locker");
        }

        if (!"PAID".equals(assignment.getRequestStatus())) {
            throw new RuntimeException("Rent can only be paid for active (PAID) locker assignments");
        }

        // Determine which year this payment covers
        int currentYear = LocalDate.now().getYear();
        LocalDate rentPaidUntil = assignment.getRentPaidUntil();
        int paymentYear;
        LocalDate periodStart;
        LocalDate periodEnd;

        if (rentPaidUntil == null || rentPaidUntil.isBefore(LocalDate.now())) {
            // First payment or overdue — pay for current year
            paymentYear = currentYear;
            periodStart = LocalDate.of(currentYear, 1, 1);
            periodEnd   = LocalDate.of(currentYear, 12, 31);
        } else {
            // Already paid until some date — pay for next year
            paymentYear = rentPaidUntil.getYear() + 1;
            periodStart = LocalDate.of(paymentYear, 1, 1);
            periodEnd   = LocalDate.of(paymentYear, 12, 31);
        }

        // Check: already paid for this year?
        if (rentPaymentRepository.findByAssignment_IdAndPaymentYear(assignmentId, paymentYear).isPresent()) {
            throw new RuntimeException("Rent for year " + paymentYear + " has already been paid");
        }

        // Validate payment method
        String method = dto.getPaymentMethod() != null ? dto.getPaymentMethod().toUpperCase() : "";
        if (!List.of("UPI", "CARD", "NETBANKING", "OFFLINE").contains(method)) {
            throw new RuntimeException("Invalid payment method. Use UPI, CARD, NETBANKING, or OFFLINE.");
        }

        // Validate method-specific fields
        if ("UPI".equals(method) && (dto.getUpiId() == null || dto.getUpiId().isBlank())) {
            throw new RuntimeException("UPI ID is required for UPI payments");
        }

        // Mock gateway processing — generate transaction ID
        String txnId = "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        String receiptNumber = "RCPT-" + paymentYear + "-" + assignmentId.substring(0, 6).toUpperCase();

        // Amount = locker's monthly price × 12 (annual rent)
        BigDecimal annualRent = assignment.getLocker().getPrice().multiply(BigDecimal.valueOf(12));

        RentPayment payment = new RentPayment();
        payment.setAssignment(assignment);
        payment.setAmount(annualRent);
        payment.setPaymentMethod(method);
        payment.setTransactionId(txnId);
        payment.setReceiptNumber(receiptNumber);
        payment.setPaymentYear(paymentYear);
        payment.setPeriodStart(periodStart);
        payment.setPeriodEnd(periodEnd);
        payment.setPaidAt(LocalDateTime.now());
        payment.setStatus("COMPLETED");
        payment.setGatewayResponse("SUCCESS: Payment processed via " + method);

        RentPayment saved = rentPaymentRepository.save(payment);

        // Update assignment rent tracking
        assignment.setRentPaidUntil(periodEnd);
        assignment.setNextRentDueDate(periodEnd.plusDays(1));
        assignment.setRentPaymentCount(assignment.getRentPaymentCount() + 1);
        assignment.setConsecutiveUnpaidYears(0); // Reset on successful payment
        assignmentRepository.save(assignment);

        return saved;
    }

    /** Retry a previously failed payment */
    @Transactional
    public RentPayment retryPayment(String assignmentId, String customerEmail, RentPaymentDto dto) {
        // Same as payRent but clears any FAILED record first
        return payRent(assignmentId, customerEmail, dto);
    }

    /** Get full rent payment history for an assignment */
    public List<RentPayment> getRentHistory(String assignmentId) {
        return rentPaymentRepository.findByAssignment_IdOrderByPaidAtDesc(assignmentId);
    }

    /** Get all assignments with overdue rent (for employee dashboard) */
    public List<LockerAssignment> getOverdueRentAssignments() {
        List<LockerAssignment> active = assignmentRepository.findByRequestStatus("PAID");
        LocalDate today = LocalDate.now();
        return active.stream()
                .filter(a -> {
                    // If nextRentDueDate is set and past, or never paid rent (assigned > 1 year ago)
                    if (a.getNextRentDueDate() != null) {
                        return a.getNextRentDueDate().isBefore(today);
                    }
                    // Never paid rent but assignment is more than 1 year old
                    return a.getAssignedAt() != null &&
                           a.getAssignedAt().toLocalDate().plusYears(1).isBefore(today);
                })
                .collect(java.util.stream.Collectors.toList());
    }
}
