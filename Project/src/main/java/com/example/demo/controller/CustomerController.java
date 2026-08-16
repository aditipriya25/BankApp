package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.BankAccount;
import com.example.demo.model.Customer;
import com.example.demo.service.BankAccountService;
import com.example.demo.service.CustomerService;

@RestController
@RequestMapping("/api/bank-customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;
    
    @Autowired
    private BankAccountService bankAccountService;

    @GetMapping("/{id}")
    public Customer getCustomer(@PathVariable String id) {
        return customerService.getCustomer(id);
    }

   
//    public Customer addCustomer(@RequestBody Customer c) {
//        return customerService.addCustomer(c);
//    }
    
    @PostMapping("/{id}/addAccount")
    public void addAccountCustomer(@RequestBody BankAccount bk,@PathVariable String id) {
    	Customer c=customerService.getCustomer(id);
    	bk.setCustomer(c);
    	BankAccount newAccount=bankAccountService.addAccount(bk);
    	c.getBankAccounts().add(newAccount);
    	
    	
    }
    
    
}