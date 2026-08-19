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
		String encodedPassword = passwordEncoder.encode(c.getPassword());
		c.setPassword(encodedPassword);
		return customerRepo.save(c);
	}

}
