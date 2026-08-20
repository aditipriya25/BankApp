package com.example.demo.service;

import com.example.demo.dto.NomineeDto;
import com.example.demo.model.LockerAssignment;
import com.example.demo.model.Nominee;
import com.example.demo.repository.LockerAssignmentRepository;
import com.example.demo.repository.NomineeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for locker nominee management.
 * RBI Circular DOR.LEG.REC/40/09.07.005/2021-22, Section 5.1:
 * "Banks shall offer nomination facility in case of safe deposit lockers...
 *  in accordance with the provisions of section 45-ZC to 45-ZF of the Banking Regulation Act, 1949"
 */
@Service
public class NomineeService {

    private final NomineeRepository nomineeRepository;
    private final LockerAssignmentRepository assignmentRepository;

    public NomineeService(NomineeRepository nomineeRepository,
                          LockerAssignmentRepository assignmentRepository) {
        this.nomineeRepository = nomineeRepository;
        this.assignmentRepository = assignmentRepository;
    }

    /**
     * Add a nominee to a locker assignment (RBI Form SL1 / SL1A).
     * Customer must own the assignment.
     */
    @Transactional
    public Nominee addNominee(String assignmentId, String customerEmail, NomineeDto dto) {
        LockerAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Locker assignment not found"));

        if (!assignment.getCustomer().getEmail().equals(customerEmail)) {
            throw new RuntimeException("You are not authorized to add a nominee to this locker");
        }

        if (!"PAID".equals(assignment.getRequestStatus()) && !"APPROVED".equals(assignment.getRequestStatus())) {
            throw new RuntimeException("Nominees can only be added to active (PAID/APPROVED) locker assignments");
        }

        // RBI 5.1.1: Check if nominee is a minor — require guardian details
        if (dto.isMinor() && (dto.getGuardianName() == null || dto.getGuardianName().isBlank())) {
            throw new RuntimeException("Guardian name is required for minor nominees as per RBI guidelines");
        }

        Nominee nominee = new Nominee();
        nominee.setAssignment(assignment);
        nominee.setName(dto.getName());
        nominee.setRelationship(dto.getRelationship());
        if (dto.getDateOfBirth() != null && !dto.getDateOfBirth().isBlank()) {
            nominee.setDateOfBirth(LocalDate.parse(dto.getDateOfBirth()));
        }
        nominee.setPhone(dto.getPhone());
        nominee.setEmail(dto.getEmail());
        nominee.setAddress(dto.getAddress());
        nominee.setPhotoUrl(dto.getPhotoUrl());
        nominee.setMinor(dto.isMinor());
        nominee.setGuardianName(dto.getGuardianName());
        nominee.setGuardianRelationship(dto.getGuardianRelationship());
        nominee.setGuardianPhone(dto.getGuardianPhone());
        // Default to SL1 if not specified
        nominee.setFormType(dto.getFormType() != null ? dto.getFormType() : "SL1");
        nominee.setActive(true);

        return nomineeRepository.save(nominee);
    }

    /** Get all active nominees for an assignment */
    public List<Nominee> getNominees(String assignmentId, String customerEmail) {
        LockerAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Locker assignment not found"));

        if (!assignment.getCustomer().getEmail().equals(customerEmail)) {
            throw new RuntimeException("Access denied");
        }
        return nomineeRepository.findByAssignment_IdAndIsActiveTrue(assignmentId);
    }

    /** Get nominees for employee view (no ownership check) */
    public List<Nominee> getNomineesForEmployee(String assignmentId) {
        return nomineeRepository.findByAssignment_IdOrderByCreatedAtAsc(assignmentId);
    }

    /**
     * Update nominee details (RBI Form SL3 / variation).
     */
    @Transactional
    public Nominee updateNominee(String nomineeId, String customerEmail, NomineeDto dto) {
        Nominee nominee = nomineeRepository.findById(nomineeId)
                .orElseThrow(() -> new RuntimeException("Nominee not found"));

        if (!nominee.getAssignment().getCustomer().getEmail().equals(customerEmail)) {
            throw new RuntimeException("Access denied");
        }

        nominee.setName(dto.getName());
        nominee.setRelationship(dto.getRelationship());
        if (dto.getDateOfBirth() != null && !dto.getDateOfBirth().isBlank()) {
            nominee.setDateOfBirth(LocalDate.parse(dto.getDateOfBirth()));
        }
        nominee.setPhone(dto.getPhone());
        nominee.setEmail(dto.getEmail());
        nominee.setAddress(dto.getAddress());
        nominee.setPhotoUrl(dto.getPhotoUrl());
        nominee.setMinor(dto.isMinor());
        nominee.setGuardianName(dto.getGuardianName());
        nominee.setGuardianRelationship(dto.getGuardianRelationship());
        nominee.setGuardianPhone(dto.getGuardianPhone());
        if (dto.getFormType() != null) nominee.setFormType(dto.getFormType());

        return nomineeRepository.save(nominee);
    }

    /**
     * Cancel nomination (RBI Form SL2 — cancellation).
     */
    @Transactional
    public void deleteNominee(String nomineeId, String customerEmail) {
        Nominee nominee = nomineeRepository.findById(nomineeId)
                .orElseThrow(() -> new RuntimeException("Nominee not found"));

        if (!nominee.getAssignment().getCustomer().getEmail().equals(customerEmail)) {
            throw new RuntimeException("Access denied");
        }

        // Soft delete — mark as inactive (SL2 cancellation form)
        nominee.setActive(false);
        nomineeRepository.save(nominee);
    }
}
