package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import com.example.demo.security.JwtAuthenticationFilter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .authorizeHttpRequests(auth -> auth

                        // ── Public endpoints ───────────────────────────────────────────────────
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/api/bank-customers/addCustomer").permitAll()
                        .requestMatchers("/api/bank-employees/addEmployee").permitAll()

                        // ── Customer endpoints ─────────────────────────────────────────────────
                        .requestMatchers("/api/bank-customers/**").hasRole("CUSTOMER")
                        .requestMatchers("/api/locker-assignments/request").hasRole("CUSTOMER")
                        .requestMatchers("/api/locker-assignments/my-assignment").hasRole("CUSTOMER")
                        .requestMatchers("/api/locker-assignments/my-assignments").hasRole("CUSTOMER")
                        .requestMatchers("/api/locker-assignments/*/pay").hasRole("CUSTOMER")
                        .requestMatchers("/api/slot-bookings/my-bookings").hasRole("CUSTOMER")

                        // ── Employee endpoints ─────────────────────────────────────────────────
                        .requestMatchers("/api/bank-employees/**").hasRole("EMPLOYEE")
                        .requestMatchers("/api/locker-assignments/pending").hasRole("EMPLOYEE")
                        .requestMatchers("/api/locker-assignments/awaiting-payment").hasRole("EMPLOYEE")
                        .requestMatchers("/api/locker-assignments/approved").hasRole("EMPLOYEE")
                        .requestMatchers("/api/locker-assignments/rejected").hasRole("EMPLOYEE")
                        .requestMatchers("/api/locker-assignments/*/approve").hasRole("EMPLOYEE")
                        .requestMatchers("/api/locker-assignments/*/reject").hasRole("EMPLOYEE")
                        .requestMatchers("/api/visit-logs/**").hasRole("EMPLOYEE")

                        // ── Shared locker endpoints ────────────────────────────────────────────
                        .requestMatchers("/api/lockers/available").hasAnyRole("CUSTOMER", "EMPLOYEE")

                        // ── KYC Endpoints ──────────────────────────────────────────────────────
                        // Customer submits KYC
                        .requestMatchers(HttpMethod.POST, "/api/kyc/submit/**").hasRole("CUSTOMER")
                        // Customer checks their own KYC status
                        .requestMatchers(HttpMethod.GET, "/api/kyc/status/**").hasRole("CUSTOMER")
                        // Employee views all pending / all KYC
                        .requestMatchers(HttpMethod.GET, "/api/kyc/pending").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.GET, "/api/kyc/all").hasRole("EMPLOYEE")
                        // Employee reviews (approve/reject) a KYC document
                        .requestMatchers(HttpMethod.PUT, "/api/kyc/*/review").hasRole("EMPLOYEE")

                        // ── Nominee Endpoints ──────────────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/nominees/employee/**").hasRole("EMPLOYEE")
                        .requestMatchers("/api/nominees/**").hasAnyRole("CUSTOMER", "EMPLOYEE")

                        // ── Agreement Endpoints ────────────────────────────────────────────────
                        // Customer signs their agreement
                        .requestMatchers(HttpMethod.POST, "/api/agreements/*/sign").hasRole("CUSTOMER")
                        // Employee generates or renews an agreement (must be before wildcard GET)
                        .requestMatchers(HttpMethod.POST, "/api/agreements/*/renew").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.POST, "/api/agreements/*").hasRole("EMPLOYEE")
                        // Both can view agreements
                        .requestMatchers(HttpMethod.GET, "/api/agreements/**").hasAnyRole("CUSTOMER", "EMPLOYEE")

                        // ── Rent Payment Endpoints ─────────────────────────────────────────────
                        .requestMatchers("/api/rent/overdue").hasRole("EMPLOYEE")
                        .requestMatchers("/api/rent/**").hasAnyRole("CUSTOMER", "EMPLOYEE")

                        // ── Locker Closure Endpoints ───────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET,  "/api/closure/pending").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.GET,  "/api/closure/all").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.PUT,  "/api/closure/*/approve").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.PUT,  "/api/closure/*/reject").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.PUT,  "/api/closure/*/complete").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.POST, "/api/closure/*/non-payment").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.POST, "/api/closure/*/law-enforcement").hasRole("EMPLOYEE")
                        .requestMatchers("/api/closure/**").hasAnyRole("CUSTOMER", "EMPLOYEE")

                        // ── Notification Endpoints ─────────────────────────────────────────────
                        .requestMatchers("/api/notifications/**").authenticated()

                        // ── Chatbot ────────────────────────────────────────────────────────────
                        .requestMatchers("/api/chatbot/health").permitAll()
                        .requestMatchers("/api/chatbot/**").authenticated()

                        .anyRequest().authenticated())

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:8000",
                "http://localhost:8081",
                "http://localhost:8082"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
