package com.example.attendancesystem.repository;

import com.example.attendancesystem.entity.Attendance;
import com.example.attendancesystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByUserOrderByRecordDateDesc(User user);
    Optional<Attendance> findByUserAndRecordDate(User user, LocalDate recordDate);
    List<Attendance> findByUserId(Long userId);
    List<Attendance> findByRecordDateBetween(LocalDate startDate, LocalDate endDate);
    List<Attendance> findByUserIdAndRecordDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
}
