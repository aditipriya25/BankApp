package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "locker")
public class Locker {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "locker_number", unique = true, nullable = false)
    private String lockerNumber;
    
    @Column(name = "locker_size")
    private String size;

    private String status;

    public Locker() {
    }

    public Locker(String id, String lockerNumber, String size, String status) {
        this.id = id;
        this.lockerNumber = lockerNumber;
        this.size = size;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLockerNumber() {
        return lockerNumber;
    }

    public void setLockerNumber(String lockerNumber) {
        this.lockerNumber = lockerNumber;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}