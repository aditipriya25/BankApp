package com.example.demo.dto;

public class PaymentRequestDto {

    private String paymentMethod;

    public PaymentRequestDto() {
    }

    public PaymentRequestDto(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
