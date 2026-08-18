package com.example.demo.dto;

public class LockerRequestDto {

    private String lockerId;

    public LockerRequestDto() {
    }

    public LockerRequestDto(String lockerId) {
        this.lockerId = lockerId;
    }

    public String getLockerId() {
        return lockerId;
    }

    public void setLockerId(String lockerId) {
        this.lockerId = lockerId;
    }
}
