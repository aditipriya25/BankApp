package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.model.Customer;
import com.example.demo.repository.CustomerRepository;

@Service
public class CustomerService {
	@Autowired
	private CustomerRepository customerRepo;

	@Autowired
	private PasswordEncoder passwordEncoder;

	public Customer getCustomer(String id) {
		return customerRepo.findById(id)
				.orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
	}

	public Customer getCustomerByEmail(String email) {
		return customerRepo.findByEmail(email)
				.orElseThrow(() -> new RuntimeException("Customer not found with email: " + email));
	}

	public Customer addCustomer(Customer c) {
		// Enforce minimum password length of 8 characters
		if (c.getPassword() == null || c.getPassword().length() < 8) {
			throw new RuntimeException("Password must be at least 8 characters long.");
		}
		// Check for duplicate email
		if (customerRepo.findByEmail(c.getEmail()).isPresent()) {
			throw new RuntimeException("An account with this email already exists.");
		}
		String encodedPassword = passwordEncoder.encode(c.getPassword());
		c.setPassword(encodedPassword);
		return customerRepo.save(c);
	}

}
