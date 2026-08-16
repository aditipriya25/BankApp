package com.example.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.VisitLog;
import com.example.demo.service.VisitLogService;
import com.example.demo.service.EmployeeService;

@RestController
@RequestMapping("/api/visit-logs")
public class VisitLogController {

    private final VisitLogService visitLogService;
    private final EmployeeService employeeService;

    public VisitLogController(
            VisitLogService visitLogService,
            EmployeeService employeeService) {

        this.visitLogService = visitLogService;
        this.employeeService = employeeService;
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<VisitLog> verifyOtp(
            @RequestBody OtpRequest request,
            Authentication authentication) {

        String employeeEmail = authentication.getName();

        String employeeId = employeeService
                .getEmployeeByEmail(employeeEmail)
                .getId();

        return ResponseEntity.ok(
                visitLogService.verifyOtp(
                        request.getBookingId(),
                        request.getOtpCode(),
                        employeeId));
    }

    @PostMapping("/{visitLogId}/return-key")
    public ResponseEntity<VisitLog> returnKey(
            @PathVariable String visitLogId) {

        return ResponseEntity.ok(
                visitLogService.returnKey(visitLogId));
    }

    public static class OtpRequest {

        private String bookingId;
        private String otpCode;

        public String getBookingId() {
            return bookingId;
        }

        public void setBookingId(String bookingId) {
            this.bookingId = bookingId;
        }

        public String getOtpCode() {
            return otpCode;
        }

        public void setOtpCode(String otpCode) {
            this.otpCode = otpCode;
        }
    }

    @GetMapping
    public ResponseEntity<List<VisitLog>> getAllVisitLogs() {
        return ResponseEntity.ok(
                visitLogService.getAllVisitLogs());
    }
}