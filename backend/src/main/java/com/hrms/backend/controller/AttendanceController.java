package com.hrms.backend.controller;

import com.hrms.backend.entity.AttendanceRecord;
import com.hrms.backend.entity.Employee;
import com.hrms.backend.entity.User;
import com.hrms.backend.repository.AttendanceRecordRepository;
import com.hrms.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceRecordRepository attendanceRepo;

    @Autowired
    private UserRepository userRepo;

    private Employee getCurrentEmployee(Principal principal) {
        User user = userRepo.findByEmail(principal.getName()).orElseThrow();
        return user.getEmployee();
    }

    @PostMapping("/check-in")
    @PreAuthorize("hasAuthority('ATTENDANCE_MARK')")
    public ResponseEntity<?> checkIn(Principal principal) {
        Employee emp = getCurrentEmployee(principal);
        LocalDate today = LocalDate.now();

        if (attendanceRepo.findByEmployeeAndAttendanceDate(emp, today).isPresent()) {
            return ResponseEntity.badRequest().body("Already checked in today.");
        }

        AttendanceRecord record = new AttendanceRecord();
        record.setEmployee(emp);
        record.setAttendanceDate(today);
        LocalDateTime now = LocalDateTime.now();
        record.setCheckIn(now);

        // Status calculation
        String status = "PRESENT";
        if (emp.getShift() != null) {
            LocalDateTime expectedStartTime = today.atTime(emp.getShift().getStartTime());
            expectedStartTime = expectedStartTime.plusMinutes(emp.getShift().getGraceMinutes() != null ? emp.getShift().getGraceMinutes() : 0);
            if (now.isAfter(expectedStartTime)) {
                status = "LATE";
            }
        }
        record.setStatus(status);

        return ResponseEntity.ok(attendanceRepo.save(record));
    }

    @PostMapping("/check-out")
    @PreAuthorize("hasAuthority('ATTENDANCE_MARK')")
    public ResponseEntity<?> checkOut(Principal principal) {
        Employee emp = getCurrentEmployee(principal);
        LocalDate today = LocalDate.now();

        AttendanceRecord record = attendanceRepo.findByEmployeeAndAttendanceDate(emp, today)
                .orElse(null);

        if (record == null || record.getCheckIn() == null) {
            return ResponseEntity.badRequest().body("No check-in found for today.");
        }

        LocalDateTime now = LocalDateTime.now();
        record.setCheckOut(now);

        long workedMinutes = ChronoUnit.MINUTES.between(record.getCheckIn(), now);

        if (emp.getShift() != null) {
            long shiftMinutes = ChronoUnit.MINUTES.between(emp.getShift().getStartTime(), emp.getShift().getEndTime());
            if (shiftMinutes < 0) shiftMinutes += 24 * 60; // handle cross-midnight if needed

            if (workedMinutes < shiftMinutes / 2) {
                record.setStatus("HALF_DAY");
            }

            LocalDateTime expectedEndTime = today.atTime(emp.getShift().getEndTime());
            long overtime = ChronoUnit.MINUTES.between(expectedEndTime, now);
            record.setOvertimeMinutes(overtime > 0 ? (int) overtime : 0);
        }

        return ResponseEntity.ok(attendanceRepo.save(record));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('ATTENDANCE_MARK')")
    public List<AttendanceRecord> getMyAttendance(Principal principal, @RequestParam(defaultValue = "") String month) {
        Employee emp = getCurrentEmployee(principal);
        if (month.isEmpty()) {
            month = LocalDate.now().toString().substring(0, 7);
        }
        return attendanceRepo.findByEmployeeIdAndMonth(emp.getEmployeeId(), month);
    }

    @GetMapping("/team")
    @PreAuthorize("hasAuthority('TIMESHEET_REVIEW_TEAM')") // Using same team review permission
    public List<AttendanceRecord> getTeamAttendance(Principal principal, @RequestParam(defaultValue = "") String date) {
        Employee emp = getCurrentEmployee(principal);
        LocalDate targetDate = date.isEmpty() ? LocalDate.now() : LocalDate.parse(date);
        return attendanceRepo.findByManagerIdAndDate(emp.getEmployeeId(), targetDate);
    }
}
