package com.example.attendancesystem.service;

import com.example.attendancesystem.entity.Attendance;
import com.example.attendancesystem.entity.FixRequest;
import com.example.attendancesystem.entity.User;
import com.example.attendancesystem.repository.AttendanceRepository;
import com.example.attendancesystem.repository.FixRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FixRequestService {

    private final FixRequestRepository fixRequestRepository;
    private final AttendanceRepository attendanceRepository;

    public FixRequestService(FixRequestRepository fixRequestRepository, AttendanceRepository attendanceRepository) {
        this.fixRequestRepository = fixRequestRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @Transactional
    public FixRequest createFixRequest(User user, Long attendanceId, LocalDateTime newCheckInTime, LocalDateTime newCheckOutTime, LocalDateTime newBreakStartTime, LocalDateTime newBreakEndTime, String reason) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new IllegalArgumentException("Attendance record not found"));

        FixRequest fixRequest = new FixRequest();
        fixRequest.setUser(user);
        fixRequest.setAttendance(attendance);
        fixRequest.setRequestDate(LocalDate.now());
        fixRequest.setNewCheckInTime(newCheckInTime);
        fixRequest.setNewCheckOutTime(newCheckOutTime);
        fixRequest.setNewBreakStartTime(newBreakStartTime);
        fixRequest.setNewBreakEndTime(newBreakEndTime);
        fixRequest.setReason(reason);
        fixRequest.setStatus("pending");

        return fixRequestRepository.save(fixRequest);
    }

    public List<FixRequest> getAllPendingFixRequests() {
        return fixRequestRepository.findByStatus("pending");
    }

    @Transactional
    public void approveFixRequest(Long requestId) {
        FixRequest fixRequest = fixRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Fix request not found"));

        if (!"pending".equals(fixRequest.getStatus())) {
            throw new IllegalStateException("Fix request is not pending.");
        }

        Attendance attendance = fixRequest.getAttendance();
        attendance.setCheckInTime(fixRequest.getNewCheckInTime());
        attendance.setCheckOutTime(fixRequest.getNewCheckOutTime());
        attendance.setBreakStartTime(fixRequest.getNewBreakStartTime());
        attendance.setBreakEndTime(fixRequest.getNewBreakEndTime());
        attendance.setStatus("fixed"); // Mark as fixed

        attendanceRepository.save(attendance);

        fixRequest.setStatus("approved");
        fixRequestRepository.save(fixRequest);
    }

    @Transactional
    public void rejectFixRequest(Long requestId) {
        FixRequest fixRequest = fixRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Fix request not found"));

        if (!"pending".equals(fixRequest.getStatus())) {
            throw new IllegalStateException("Fix request is not pending.");
        }

        fixRequest.setStatus("rejected");
        fixRequestRepository.save(fixRequest);
    }

    public Optional<FixRequest> getFixRequestById(Long requestId) {
        return fixRequestRepository.findById(requestId);
    }
}
