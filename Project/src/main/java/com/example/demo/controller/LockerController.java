package com.example.demo.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.Locker;
import com.example.demo.service.LockerService;

@RestController
@RequestMapping("/api/lockers")
public class LockerController {

    @Autowired
    private LockerService lockerService;

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public Locker addLocker(@RequestBody Locker locker) {
        return lockerService.addLocker(locker);
    }

    @GetMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public List<Locker> getAllLockers() {
        return lockerService.getAllLockers();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public Locker getLocker(@PathVariable String id) {
        return lockerService.getLocker(id);
    }

    @GetMapping("/available")
    public List<Locker> getAvailableLockers(
            @RequestParam(required = false) String size,
            @RequestParam(required = false) BigDecimal maxPrice) {
        return lockerService.getAvailableLockers(size, maxPrice);
    }
}
