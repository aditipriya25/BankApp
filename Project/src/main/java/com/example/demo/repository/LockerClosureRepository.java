package com.example.demo.repository;

import com.example.demo.model.LockerClosure;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LockerClosureRepository extends JpaRepository<LockerClosure, String> {
    List<LockerClosure> findByStatusNot(String status);
    List<LockerClosure> findByStatus(String status);
    List<LockerClosure> findByClosureType(String closureType);
    Optional<LockerClosure> findByAssignment_Id(String assignmentId);
    List<LockerClosure> findByAssignment_IdIn(List<String> assignmentIds);
}
