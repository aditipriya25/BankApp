package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Customer;
import com.example.demo.model.Employee;
import com.example.demo.service.CustomerService;
import com.example.demo.service.EmployeeService;

@RestController
@RequestMapping("/api/bank-employees")
public class EmployeeController {
	@Autowired
	private EmployeeService employeeService;
	
	@Autowired
	private CustomerService customerService;
	@GetMapping("/{id}")
	public Employee getEmployee(@PathVariable String id) {
		return employeeService.getEmployee(id);
	}
	
	@PostMapping("/addEmployee")
	public Employee addEmployee(@RequestBody Employee employee) {
//		System.out.println(employee);
		return employeeService.addEmployee(employee);
	}
	
	@PostMapping("/{id}/addCustomer")
	public void addCustomerToEmployee(@RequestBody Customer c,@PathVariable String id) {
		Employee e=employeeService.getEmployee(id);
		c.setEmployee(e);
		Customer newCust=customerService.addCustomer(c);
		e.getCustomers().add(newCust);
	}
	
	
}
