package com.example.attendancesystem.repository;

import com.example.attendancesystem.entity.FixRequest;
import com.example.attendancesystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FixRequestRepository extends JpaRepository<FixRequest, Long> {
    List<FixRequest> findByUser(User user);
    List<FixRequest> findByStatus(String status);
}
