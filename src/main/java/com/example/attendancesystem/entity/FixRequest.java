package com.example.attendancesystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fix_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FixRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "attendance_id")
    private Attendance attendance;

    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;

    @Column(name = "new_check_in_time")
    private LocalDateTime newCheckInTime;

    @Column(name = "new_check_out_time")
    private LocalDateTime newCheckOutTime;

    @Column(name = "new_break_start_time")
    private LocalDateTime newBreakStartTime;

    @Column(name = "new_break_end_time")
    private LocalDateTime newBreakEndTime;

    @Column(nullable = false)
    private String reason;

    private String status = "pending"; // default status
}
