package com.example.attendancesystem.controller;

import com.example.attendancesystem.entity.Attendance;
import com.example.attendancesystem.entity.FixRequest;
import com.example.attendancesystem.entity.User;
import com.example.attendancesystem.service.AttendanceService;
import com.example.attendancesystem.service.FixRequestService;
import com.example.attendancesystem.repository.UserRepository; // Corrected package name
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')") // Ensure only ADMIN role can access this controller
public class AdminController {

    private final AttendanceService attendanceService;
    private final FixRequestService fixRequestService;
    private final UserRepository userRepository;

    public AdminController(AttendanceService attendanceService, FixRequestService fixRequestService, UserRepository userRepository) {
        this.attendanceService = attendanceService;
        this.fixRequestService = fixRequestService;
        this.userRepository = userRepository;
    }

    @GetMapping("/attendance")
    public String listAllAttendance(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "startDate", required = false) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) LocalDate endDate,
            Model model) {

        List<Attendance> attendanceRecords;
        if (userId != null && startDate != null && endDate != null) {
            attendanceRecords = attendanceService.getAttendanceByUserIdAndDateRange(userId, startDate, endDate);
        } else if (startDate != null && endDate != null) {
            attendanceRecords = attendanceService.getAttendanceByDateRange(startDate, endDate);
        } else if (userId != null) {
            attendanceRecords = attendanceService.getAttendanceByUserId(userId);
        } else {
            attendanceRecords = attendanceService.getAllAttendance();
        }

        model.addAttribute("allAttendanceRecords", attendanceRecords);
        model.addAttribute("users", userRepository.findAll()); // For filter dropdown
        return "admin_dashboard";
    }

    @GetMapping("/fix-requests")
    public String listFixRequests(Model model) {
        model.addAttribute("fixRequests", fixRequestService.getAllPendingFixRequests());
        return "fix_request_list";
    }

    @PostMapping("/fix-requests/{id}/approve")
    public String approveFixRequest(@PathVariable("id") Long requestId) {
        fixRequestService.approveFixRequest(requestId);
        return "redirect:/admin/fix-requests?success=approved";
    }

    @PostMapping("/fix-requests/{id}/reject")
    public String rejectFixRequest(@PathVariable("id") Long requestId) {
        fixRequestService.rejectFixRequest(requestId);
        return "redirect:/admin/fix-requests?success=rejected";
    }

    @GetMapping("/alerts")
    public String showAnomalyDetection(Model model) {
        model.addAttribute("anomalies", attendanceService.detectAnomalies());
        return "anomaly_detection";
    }

    @GetMapping("/attendance/csv")
    public void exportAttendanceCsv(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "startDate", required = false) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) LocalDate endDate,
            HttpServletResponse response) throws IOException {

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"attendance_records.csv\"");

        List<Attendance> records = attendanceService.getAllAttendance(); // Initialize here

        // Apply filters
        if (userId != null) {
            records = records.stream()
                             .filter(att -> att.getUser().getId().equals(userId))
                             .collect(Collectors.toList());
        }
        if (startDate != null) {
            records = records.stream()
                             .filter(att -> att.getRecordDate() != null && !att.getRecordDate().isBefore(startDate))
                             .collect(Collectors.toList());
        }
        if (endDate != null) {
            records = records.stream()
                             .filter(att -> att.getRecordDate() != null && !att.getRecordDate().isAfter(endDate))
                             .collect(Collectors.toList());
        }

        try (PrintWriter writer = response.getWriter()) {
            writer.append("ID,ユーザー名,日付,出勤,退勤,休憩開始,休憩終了,場所,ステータス\n");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (Attendance record : records) {
                writer.append(String.format("%d,%s,%s,%s,%s,%s,%s,%s,%s\n",
                        record.getId(),
                        record.getUser().getUsername(),
                        record.getRecordDate(),
                        record.getCheckInTime() != null ? record.getCheckInTime().format(formatter) : "",
                        record.getCheckOutTime() != null ? record.getCheckOutTime().format(formatter) : "",
                        record.getBreakStartTime() != null ? record.getBreakStartTime().format(formatter) : "",
                        record.getBreakEndTime() != null ? record.getBreakEndTime().format(formatter) : "",
                        record.getLocation() != null ? record.getLocation() : "",
                        record.getStatus()
                ));
            }
        }
    }
}