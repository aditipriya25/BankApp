package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // @PostMapping("/login")
    // public String login(@RequestBody LoginRequest request) {

    // authService.authenticateCustomer(
    // request.getEmail(),
    // request.getPassword());

    // return "Login successful";
    // }
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {

        String token = authService.login(
                request.getEmail(),
                request.getPassword());

        return new LoginResponse(token);
    }
}