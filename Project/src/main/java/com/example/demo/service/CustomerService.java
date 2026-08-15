package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.model.Customer;
import com.example.demo.model.Employee;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.EmployeeRepository;

@Service
public class CustomerService {
	@Autowired
	private CustomerRepository customerRepo;
	
	public Customer getCustomer(String id) {
		Customer c=customerRepo.findById(id).get();
		return c;
		
	}
	
	public Customer addCustomer(Customer c) {
		return customerRepo.save(c);
	}
	
}
