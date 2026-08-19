package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.model.Customer;
import com.example.demo.model.Employee;
import com.example.demo.repository.EmployeeRepository;

@Service
public class EmployeeService {

	@Autowired
	private EmployeeRepository employeeRepo;

	@Autowired
	private PasswordEncoder passwordEncoder;

	public Employee getEmployee(String id) {
		Employee e = employeeRepo.findById(id).get();
		return e;
	}

	public Employee addEmployee(Employee employee) {
		// Normalize role to uppercase so JWT role claims always match frontend expectations
		if (employee.getRole() == null || employee.getRole().isBlank()) {
			employee.setRole("EMPLOYEE");
		} else {
			employee.setRole(employee.getRole().toUpperCase());
		}
		String encodedPassword = passwordEncoder.encode(employee.getPassword());
		employee.setPassword(encodedPassword);
		return employeeRepo.save(employee);
	}

	public Employee getEmployeeByEmail(String email) {
		return employeeRepo.findByEmail(email)
				.orElseThrow(() -> new RuntimeException("Employee not found"));
	}
}
