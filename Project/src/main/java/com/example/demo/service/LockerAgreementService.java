package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Manages locker agreements per RBI para 2.1.
 * "Banks shall have a Board approved agreement for safe deposit lockers.
 *  Banks shall renew their locker agreements with existing locker customers by January 1, 2023."
 */
@Service
public class LockerAgreementService {

    private final LockerAgreementRepository agreementRepository;
    private final LockerAssignmentRepository assignmentRepository;
    private final EmployeeRepository employeeRepository;

    public LockerAgreementService(LockerAgreementRepository agreementRepository,
                                  LockerAssignmentRepository assignmentRepository,
                                  EmployeeRepository employeeRepository) {
        this.agreementRepository = agreementRepository;
        this.assignmentRepository = assignmentRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Generate the model locker agreement text (IBA template as per RBI 2.1.1).
     */
    private String generateAgreementContent(LockerAssignment assignment) {
        String customerName = assignment.getCustomer().getFullName();
        String lockerNumber = assignment.getLocker().getLockerNumber();
        String lockerSize   = assignment.getLocker().getSize();
        String price        = assignment.getLocker().getPrice().toPlainString();

        return "SAFE DEPOSIT LOCKER AGREEMENT\n" +
               "================================\n" +
               "Reference: RBI Circular DOR.LEG.REC/40/09.07.005/2021-22\n\n" +
               "This Agreement is entered into between:\n\n" +
               "BANK (Hereafter \"the Bank\") AND\n" +
               "LOCKER-HIRER: " + customerName + " (Hereafter \"the Customer\")\n\n" +
               "1. LOCKER DETAILS\n" +
               "   Locker Number: " + lockerNumber + "\n" +
               "   Locker Size: " + lockerSize + "\n" +
               "   Annual Rent: INR " + price + "\n\n" +
               "2. TERMS AND CONDITIONS\n" +
               "   2.1 The Customer shall not keep anything illegal or any hazardous substance in the Safe Deposit Locker (RBI para 1.3).\n" +
               "   2.2 Locker rent is payable annually. Non-payment for 3 consecutive years may result in forced closure (RBI para 6.3.1).\n" +
               "   2.3 The Bank shall send email/SMS alerts after every locker access (RBI para 4.1.3).\n" +
               "   2.4 The Customer may nominate a person per Section 45-ZC to 45-ZF of the Banking Regulation Act, 1949.\n" +
               "   2.5 The Bank's liability for fire/theft/burglary due to bank negligence shall be 100x annual rent (RBI para 7.2).\n" +
               "   2.6 The Bank is not liable for natural calamities (RBI para 7.1).\n" +
               "   2.7 The Bank shall not insure the contents of the locker (RBI para 8.2).\n" +
               "   2.8 If the locker remains inoperative for 7 years, the Bank may transfer contents to nominee/legal heir (RBI para 6.4.1).\n\n" +
               "3. CUSTOMER OBLIGATIONS\n" +
               "   3.1 Operate the locker only with the key/password provided by the Bank.\n" +
               "   3.2 Notify the Bank immediately if the key is lost.\n" +
               "   3.3 Pay locker rent promptly each year.\n" +
               "   3.4 Comply with all KYC requirements on an ongoing basis.\n\n" +
               "4. ACKNOWLEDGEMENT\n" +
               "   By signing this agreement, both parties agree to the above terms as per RBI guidelines and\n" +
               "   the Banking Regulation Act, 1949.\n\n" +
               "   Date: " + LocalDate.now() + "\n";
    }

    /**
     * Create a new agreement when employee approves a locker request (RBI 2.1.2).
     */
    @Transactional
    public LockerAgreement createAgreement(String assignmentId, String employeeEmail) {
        LockerAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        // Check if agreement already exists
        Optional<LockerAgreement> existing = agreementRepository.findByAssignment_Id(assignmentId);
        if (existing.isPresent()) {
            return existing.get(); // Return existing agreement
        }

        Employee employee = employeeRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        LockerAgreement agreement = new LockerAgreement();
        agreement.setAssignment(assignment);
        agreement.setAgreementDate(LocalDate.now());
        agreement.setAgreementContent(generateAgreementContent(assignment));
        agreement.setStampDutyPaid(true);
        agreement.setStampDutyAmount(100.0);  // Standard stamp duty
        agreement.setSignedByEmployee(employee);
        agreement.setEmployeeSignedAt(LocalDateTime.now());
        agreement.setRenewalDue(LocalDate.of(2023, 1, 1)); // RBI 2.1.1 renewal deadline
        agreement.setTermsAccepted(false);
        agreement.setSignedByCustomer(false);

        return agreementRepository.save(agreement);
    }

    /** Get agreement for an assignment */
    public LockerAgreement getAgreement(String assignmentId) {
        return agreementRepository.findByAssignment_Id(assignmentId)
                .orElseThrow(() -> new RuntimeException("No agreement found for this locker. Please contact the bank."));
    }

    /**
     * Customer digitally accepts/signs the agreement (RBI 2.1.2 — copy furnished to locker-hirer).
     */
    @Transactional
    public LockerAgreement customerSignAgreement(String assignmentId, String customerEmail) {
        LockerAgreement agreement = agreementRepository.findByAssignment_Id(assignmentId)
                .orElseThrow(() -> new RuntimeException("Agreement not found"));

        if (!agreement.getAssignment().getCustomer().getEmail().equals(customerEmail)) {
            throw new RuntimeException("Access denied");
        }

        agreement.setSignedByCustomer(true);
        agreement.setCustomerSignedAt(LocalDateTime.now());
        agreement.setTermsAccepted(true);

        return agreementRepository.save(agreement);
    }

    /** Renew an existing agreement */
    @Transactional
    public LockerAgreement renewAgreement(String assignmentId, String employeeEmail) {
        LockerAgreement agreement = agreementRepository.findByAssignment_Id(assignmentId)
                .orElseThrow(() -> new RuntimeException("Agreement not found"));

        Employee employee = employeeRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        agreement.setAgreementDate(LocalDate.now());
        agreement.setAgreementContent(generateAgreementContent(agreement.getAssignment()));
        agreement.setSignedByEmployee(employee);
        agreement.setEmployeeSignedAt(LocalDateTime.now());
        agreement.setSignedByCustomer(false); // Customer must re-sign
        agreement.setCustomerSignedAt(null);
        agreement.setRenewed(true);
        agreement.setRenewalDue(LocalDate.now().plusYears(1));

        return agreementRepository.save(agreement);
    }
}
