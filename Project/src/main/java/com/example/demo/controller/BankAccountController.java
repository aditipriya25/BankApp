package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.BankAccount;
import com.example.demo.service.BankAccountService;


@RestController
@RequestMapping("/api/bank-accounts")
public class BankAccountController {
	
	@Autowired
	private BankAccountService bankAccountService;
	
	@GetMapping("/{id}")
	public BankAccount getAccount(@PathVariable String id) {
		return bankAccountService.getAccount(id);
	}
	
	@PostMapping("/addAccount")
	public BankAccount addAccount(@RequestBody BankAccount bankAccount) {
		return bankAccountService.addAccount(bankAccount);
	}
}
