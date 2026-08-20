package com.example.demo.controller;

import com.example.demo.dto.NomineeDto;
import com.example.demo.model.Nominee;
import com.example.demo.service.NomineeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nominees")
public class NomineeController {

    private final NomineeService nomineeService;

    public NomineeController(NomineeService nomineeService) {
        this.nomineeService = nomineeService;
    }

    /** Customer: Add nominee to locker (RBI Form SL1) */
    @PostMapping("/{assignmentId}")
    public ResponseEntity<Nominee> addNominee(
            @PathVariable String assignmentId,
            @RequestBody NomineeDto dto,
            Authentication auth) {
        return ResponseEntity.ok(nomineeService.addNominee(assignmentId, auth.getName(), dto));
    }

    /** Customer: Get my nominees for an assignment */
    @GetMapping("/{assignmentId}")
    public ResponseEntity<List<Nominee>> getNominees(
            @PathVariable String assignmentId,
            Authentication auth) {
        return ResponseEntity.ok(nomineeService.getNominees(assignmentId, auth.getName()));
    }

    /** Customer: Update nominee details (RBI Form SL3 — variation) */
    @PutMapping("/nominee/{nomineeId}")
    public ResponseEntity<Nominee> updateNominee(
            @PathVariable String nomineeId,
            @RequestBody NomineeDto dto,
            Authentication auth) {
        return ResponseEntity.ok(nomineeService.updateNominee(nomineeId, auth.getName(), dto));
    }

    /** Customer: Cancel nomination (RBI Form SL2) */
    @DeleteMapping("/nominee/{nomineeId}")
    public ResponseEntity<Void> deleteNominee(
            @PathVariable String nomineeId,
            Authentication auth) {
        nomineeService.deleteNominee(nomineeId, auth.getName());
        return ResponseEntity.ok().build();
    }

    /** Employee: View all nominees for any assignment */
    @GetMapping("/employee/{assignmentId}")
    public ResponseEntity<List<Nominee>> getNomineesEmployee(@PathVariable String assignmentId) {
        return ResponseEntity.ok(nomineeService.getNomineesForEmployee(assignmentId));
    }
}
