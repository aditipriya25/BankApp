package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/api/bank-customers/addCustomer").permitAll()
                        .requestMatchers("/api/bank-employees/addEmployee").permitAll()

                        .requestMatchers("/api/bank-customers/**").hasRole("CUSTOMER")
                        .requestMatchers("/api/bank-employees/**").hasRole("EMPLOYEE")
                        .requestMatchers("/api/locker-assignments/request").hasRole("CUSTOMER")
                        .requestMatchers("/api/locker-assignments/my-assignment").hasRole("CUSTOMER")
                        .requestMatchers("/api/locker-assignments/*/pay").hasRole("CUSTOMER")
                        .requestMatchers("/api/lockers/available").hasAnyRole("CUSTOMER", "EMPLOYEE")
                        .requestMatchers("/api/slot-bookings/my-bookings").hasRole("CUSTOMER")
                        .requestMatchers("/api/locker-assignments/pending").hasRole("EMPLOYEE")
                        .requestMatchers("/api/locker-assignments/awaiting-payment").hasRole("EMPLOYEE")
                        .requestMatchers("/api/locker-assignments/*/approve").hasRole("EMPLOYEE")
                        .requestMatchers("/api/locker-assignments/*/reject").hasRole("EMPLOYEE")
                        .requestMatchers("/api/visit-logs/**").hasRole("EMPLOYEE")

                        .anyRequest().authenticated())

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}