package com.example.attendancesystem.controller;

import com.example.attendancesystem.entity.Attendance;
import com.example.attendancesystem.entity.User;
import com.example.attendancesystem.repository.UserRepository;
import com.example.attendancesystem.service.AttendanceService;
import com.example.attendancesystem.service.FixRequestService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final UserRepository userRepository;
    private final FixRequestService fixRequestService;

    public AttendanceController(AttendanceService attendanceService, UserRepository userRepository, FixRequestService fixRequestService) {
        this.attendanceService = attendanceService;
        this.userRepository = userRepository;
        this.fixRequestService = fixRequestService;
    }

    @PostMapping("/punch")
    public String punch(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("type") String type) {

        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        attendanceService.punch(currentUser, type);

        return "redirect:/dashboard";
    }

    @GetMapping("/history")
    public String showAttendanceHistory(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("attendanceRecords", attendanceService.getUserAttendance(currentUser));
        return "attendance_history";
    }

    @GetMapping("/history/{id}/request-fix")
    public String showFixRequestForm(@PathVariable("id") Long attendanceId, Model model) {
        Optional<Attendance> attendance = attendanceService.getAttendanceById(attendanceId);
        if (attendance.isEmpty()) {
            return "redirect:/attendance/history"; // Or show error
        }
        model.addAttribute("attendance", attendance.get());
        return "fix_request_form";
    }

    @PostMapping("/history/{id}/request-fix")
    public String submitFixRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") Long attendanceId,
            @RequestParam("newCheckInTime") LocalDateTime newCheckInTime,
            @RequestParam("newCheckOutTime") LocalDateTime newCheckOutTime,
            @RequestParam(value = "newBreakStartTime", required = false) LocalDateTime newBreakStartTime,
            @RequestParam(value = "newBreakEndTime", required = false) LocalDateTime newBreakEndTime,
            @RequestParam("reason") String reason) {

        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        fixRequestService.createFixRequest(currentUser, attendanceId, newCheckInTime, newCheckOutTime, newBreakStartTime, newBreakEndTime, reason);

        return "redirect:/attendance/history?success=fixRequestSubmitted";
    }
}
