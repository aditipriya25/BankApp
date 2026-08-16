package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.model.Customer;
import com.example.demo.model.Employee;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.EmployeeRepository;

@Service
public class CustomerService {
	@Autowired
	private CustomerRepository customerRepo;

	@Autowired
	private PasswordEncoder passwordEncoder;

	public Customer getCustomer(String id) {
		Customer c = customerRepo.findById(id).get();
		return c;

	}

	public Customer addCustomer(Customer c) {
		String encodedPassword = passwordEncoder.encode(c.getPassword());
		c.setPassword(encodedPassword);
		return customerRepo.save(c);
	}

}
