package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.model.Customer;
import com.example.demo.model.Employee;
import com.example.demo.repository.EmployeeRepository;

@Service
public class EmployeeService {
	
	@Autowired
	private EmployeeRepository employeeRepo;
	public Employee getEmployee(String id) {
		Employee e=employeeRepo.findById(id).get();
		return e;
	}
	
	public Employee addEmployee(Employee employee) {
		return employeeRepo.save(employee);
	}
	
//	public void addCustomerToEmployee(Customer c, Employee e) {
//		e.setCustomers(e.getCustomers().add(c));
//	}
}
