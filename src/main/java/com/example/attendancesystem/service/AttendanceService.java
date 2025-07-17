package com.example.attendancesystem.service;

import com.example.attendancesystem.entity.Attendance;
import com.example.attendancesystem.entity.User;
import com.example.attendancesystem.repository.AttendanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    public AttendanceService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    @Transactional
    public Attendance punch(User user, String type) {
        LocalDate today = LocalDate.now();
        Optional<Attendance> existingAttendance = attendanceRepository.findByUserAndRecordDate(user, today);

        Attendance attendance;
        if (existingAttendance.isPresent()) {
            attendance = existingAttendance.get();
        } else {
            attendance = new Attendance();
            attendance.setUser(user);
            attendance.setRecordDate(today);
        }

        switch (type) {
            case "check_in":
                if (attendance.getCheckInTime() == null) {
                    attendance.setCheckInTime(LocalDateTime.now());
                }
                break;
            case "check_out":
                if (attendance.getCheckOutTime() == null) {
                    attendance.setCheckOutTime(LocalDateTime.now());
                }
                break;
            case "break_start":
                if (attendance.getBreakStartTime() == null) {
                    attendance.setBreakStartTime(LocalDateTime.now());
                }
                break;
            case "break_end":
                if (attendance.getBreakEndTime() == null) {
                    attendance.setBreakEndTime(LocalDateTime.now());
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid punch type: " + type);
        }

        return attendanceRepository.save(attendance);
    }

    public List<Attendance> getUserAttendance(User user) {
        return attendanceRepository.findByUserOrderByRecordDateDesc(user);
    }

    public List<Attendance> getAllAttendance() {
        return attendanceRepository.findAll();
    }

    public Optional<Attendance> getAttendanceById(Long id) {
        return attendanceRepository.findById(id);
    }

    public List<Attendance> getAttendanceByUserId(Long userId) {
        return attendanceRepository.findByUserId(userId);
    }

    public List<Attendance> getAttendanceByDateRange(LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findByRecordDateBetween(startDate, endDate);
    }

    public List<Attendance> getAttendanceByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findByUserIdAndRecordDateBetween(userId, startDate, endDate);
    }

    public List<Attendance> detectAnomalies() {
        // Simple anomaly detection: find records with missing check-out or very long hours
        return attendanceRepository.findAll().stream()
                .filter(att -> att.getCheckOutTime() == null ||
                        (att.getCheckInTime() != null && att.getCheckOutTime() != null &&
                                ChronoUnit.HOURS.between(att.getCheckInTime(), att.getCheckOutTime()) > 12)) // Example: more than 12 hours
                .toList();
    }

    @Transactional
    public Attendance saveAttendance(Attendance attendance) {
        return attendanceRepository.save(attendance);
    }
}
