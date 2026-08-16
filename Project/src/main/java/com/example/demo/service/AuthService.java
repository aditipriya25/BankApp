package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.exception.AuthenticationException;
import com.example.demo.model.Customer;
import com.example.demo.model.Employee;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.security.JwtService;

@Service
public class AuthService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    // public Customer authenticateCustomer(String email, String password) {

    // Customer customer = customerRepository.findByEmail(email)
    // .orElseThrow(() -> new RuntimeException("Invalid email or password"));

    // if (!passwordEncoder.matches(password, customer.getPassword())) {
    // throw new RuntimeException("Invalid email or password");
    // }

    // return customer;
    // }

    public String login(String email, String password) {

        // First check Customer
        var customer = customerRepository.findByEmail(email);

        if (customer.isPresent()) {

            Customer c = customer.get();

            if (!passwordEncoder.matches(password, c.getPassword())) {
                throw new AuthenticationException("Invalid email or password");
            }

            return jwtService.generateToken(
                    c.getEmail(),
                    "CUSTOMER");
        }

        // If not Customer, check Employee
        var employee = employeeRepository.findByEmail(email);

        if (employee.isPresent()) {

            Employee e = employee.get();

            if (!passwordEncoder.matches(password, e.getPassword())) {
                throw new AuthenticationException("Invalid email or password");
            }

            return jwtService.generateToken(
                    e.getEmail(),
                    e.getRole());
        }

        throw new AuthenticationException("Invalid email or password");
    }
}