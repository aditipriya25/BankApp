package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.model.BankAccount;
import com.example.demo.repository.BankAccountRepository;

@Service
public class BankAccountService {
	@Autowired
	private BankAccountRepository bankAccountRepo;
	public BankAccount getAccount(String id) {
		BankAccount bk=bankAccountRepo.findById(id).orElseThrow(() -> new RuntimeException("Bank Account not found with id: " + id));
		return bk;
	}
	
	public BankAccount addAccount(BankAccount bankAccount) {
		return bankAccountRepo.save(bankAccount);
	}
}
