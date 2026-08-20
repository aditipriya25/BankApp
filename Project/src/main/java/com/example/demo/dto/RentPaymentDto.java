package com.example.demo.dto;

public class RentPaymentDto {
    /** UPI, CARD, NETBANKING, OFFLINE */
    private String paymentMethod;
    private String upiId;           // for UPI
    private String cardNumber;      // last 4 digits for display
    private String bankName;        // for NETBANKING
    private String transactionRef;  // client-provided ref (optional)

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getUpiId() { return upiId; }
    public void setUpiId(String upiId) { this.upiId = upiId; }
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getTransactionRef() { return transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }
}
