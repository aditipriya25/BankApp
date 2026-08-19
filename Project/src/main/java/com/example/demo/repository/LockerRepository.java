package com.example.demo.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Locker;

@Repository
public interface LockerRepository extends JpaRepository<Locker, String> {

    List<Locker> findByStatus(String status);

    Optional<Locker> findByIdAndStatus(String id, String status);

    List<Locker> findByStatusAndSize(String status, String size);

    List<Locker> findByStatusAndSizeAndPriceLessThanEqual(String status, String size, BigDecimal maxPrice);

    List<Locker> findBySize(String size);
}