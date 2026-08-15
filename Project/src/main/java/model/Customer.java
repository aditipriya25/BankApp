package model;

import java.util.List;

import jakarta.persistence.*;
import jakarta.persistence.Table;

@Entity
@Table(name="customer")
public class Customer {
	
	@Id
	private String id;
	
	@Column(name="fullName")
	private String fullName;
	
	@Column(unique=true,nullable=false)
	private String email;
	
	private String password;
	private String phone;
	
	private String kycStatus;
	private List<BankAccount> bankAccounts;
	
	private List<LockerAssignment> lockerAssignments;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getKycStatus() {
		return kycStatus;
	}

	public void setKycStatus(String kycStatus) {
		this.kycStatus = kycStatus;
	}

	public List<BankAccount> getBankAccounts() {
		return bankAccounts;
	}

	public void setBankAccounts(List<BankAccount> bankAccounts) {
		this.bankAccounts = bankAccounts;
	}

	public List<LockerAssignment> getLockerAssignments() {
		return lockerAssignments;
	}

	public Customer() {
		super();
	}

	@Override
	public String toString() {
		return "Customer [id=" + id + ", fullName=" + fullName + ", email=" + email + ", password=" + password
				+ ", phone=" + phone + ", kycStatus=" + kycStatus + ", bankAccounts=" + bankAccounts
				+ ", lockerAssignments=" + lockerAssignments + "]";
	}

	public Customer(String id, String fullName, String email, String password, String phone, String kycStatus,
			List<BankAccount> bankAccounts, List<LockerAssignment> lockerAssignments) {
		super();
		this.id = id;
		this.fullName = fullName;
		this.email = email;
		this.password = password;
		this.phone = phone;
		this.kycStatus = kycStatus;
		this.bankAccounts = bankAccounts;
		this.lockerAssignments = lockerAssignments;
	}

	public void setLockerAssignments(List<LockerAssignment> lockerAssignments) {
		this.lockerAssignments = lockerAssignments;
	}
	
	
}
