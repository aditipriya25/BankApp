package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.model.Locker;
import com.example.demo.repository.LockerRepository;

@Service
public class LockerService {

    @Autowired
    private LockerRepository lockerRepository;

    public Locker addLocker(Locker locker) {
        locker.setStatus("AVAILABLE");
        return lockerRepository.save(locker);
    }

    public List<Locker> getAllLockers() {
        return lockerRepository.findAll();
    }

    public Locker getLocker(String id) {
        return lockerRepository.findById(id).orElse(null);
    }

    public List<Locker> getAvailableLockers() {
        return lockerRepository.findByStatus("AVAILABLE");
    }
}