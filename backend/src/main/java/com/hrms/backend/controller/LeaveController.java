package com.hrms.backend.controller;

import com.hrms.backend.entity.*;
import com.hrms.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/leave")
public class LeaveController {

    @Autowired private LeaveTypeRepository leaveTypeRepo;
    @Autowired private LeaveBalanceRepository leaveBalanceRepo;
    @Autowired private LeaveRequestRepository leaveRequestRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private EmployeeRepository employeeRepo;
    @Autowired private AttendanceRecordRepository attendanceRepo;

    private Employee getCurrentEmployee(Principal principal) {
        User user = userRepo.findByEmail(principal.getName()).orElseThrow();
        return user.getEmployee();
    }

    // --- LEAVE TYPES (Policies) ---
    @GetMapping("/types")
    public List<LeaveType> getLeaveTypes() {
        return leaveTypeRepo.findAll();
    }

    @PostMapping("/types")
    @PreAuthorize("hasAuthority('LEAVE_POLICY_MANAGE')")
    public LeaveType createLeaveType(@RequestBody LeaveType type) {
        return leaveTypeRepo.save(type);
    }

    @PutMapping("/types/{id}")
    @PreAuthorize("hasAuthority('LEAVE_POLICY_MANAGE')")
    public LeaveType updateLeaveType(@PathVariable Integer id, @RequestBody LeaveType updated) {
        LeaveType existing = leaveTypeRepo.findById(id).orElseThrow();
        existing.setLeaveName(updated.getLeaveName());
        existing.setDaysPerYear(updated.getDaysPerYear());
        existing.setIsPaid(updated.getIsPaid());
        return leaveTypeRepo.save(existing);
    }

    @DeleteMapping("/types/{id}")
    @PreAuthorize("hasAuthority('LEAVE_POLICY_MANAGE')")
    public void deleteLeaveType(@PathVariable Integer id) {
        leaveTypeRepo.deleteById(id);
    }

    // --- BALANCES ---
    @PostMapping("/balances/initialize")
    @PreAuthorize("hasAuthority('LEAVE_POLICY_MANAGE')")
    @Transactional
    public ResponseEntity<?> initializeBalances(@RequestParam Integer year) {
        List<Employee> activeEmployees = employeeRepo.findAll().stream()
            .filter(e -> "ACTIVE".equals(e.getStatus()))
            .toList();
        List<LeaveType> leaveTypes = leaveTypeRepo.findAll();
        int createdCount = 0;

        for (Employee emp : activeEmployees) {
            for (LeaveType lt : leaveTypes) {
                Optional<LeaveBalance> existing = leaveBalanceRepo.findByEmployee_EmployeeIdAndLeaveType_LeaveTypeIdAndBalanceYear(emp.getEmployeeId(), lt.getLeaveTypeId(), year);
                if (existing.isEmpty()) {
                    LeaveBalance bal = new LeaveBalance();
                    bal.setEmployee(emp);
                    bal.setLeaveType(lt);
                    bal.setBalanceYear(year);
                    bal.setAllocatedDays(lt.getDaysPerYear());
                    bal.setUsedDays(BigDecimal.ZERO);
                    leaveBalanceRepo.save(bal);
                    createdCount++;
                }
            }
        }
        return ResponseEntity.ok("Initialized " + createdCount + " balances for year " + year);
    }

    @GetMapping("/balances/me")
    public List<LeaveBalance> getMyBalances(Principal principal) {
        Employee emp = getCurrentEmployee(principal);
        return leaveBalanceRepo.findByEmployee_EmployeeIdAndBalanceYear(emp.getEmployeeId(), LocalDate.now().getYear());
    }

    // --- LEAVE REQUESTS ---
    @PostMapping("/requests")
    @PreAuthorize("hasAuthority('LEAVE_APPLY')")
    @Transactional
    public ResponseEntity<?> applyForLeave(@RequestBody LeaveRequest req, Principal principal) {
        Employee emp = getCurrentEmployee(principal);
        req.setEmployee(emp);
        req.setStatus("PENDING");

        // Calculate total days (inclusive)
        long days = ChronoUnit.DAYS.between(req.getStartDate(), req.getEndDate()) + 1;
        if (days <= 0) return ResponseEntity.badRequest().body("End date must be on or after start date");
        req.setTotalDays(new BigDecimal(days));

        LeaveBalance balance = leaveBalanceRepo.findByEmployee_EmployeeIdAndLeaveType_LeaveTypeIdAndBalanceYear(
            emp.getEmployeeId(), req.getLeaveType().getLeaveTypeId(), req.getStartDate().getYear()
        ).orElse(null);

        if (balance == null) return ResponseEntity.badRequest().body("No leave balance found for this type/year.");

        if (req.getLeaveType().getLeaveTypeId() != 3) {
            BigDecimal remaining = balance.getAllocatedDays().subtract(balance.getUsedDays());
            if (req.getTotalDays().compareTo(remaining) > 0) {
                return ResponseEntity.badRequest().body("Requested days exceed remaining balance.");
            }
        }

        return ResponseEntity.ok(leaveRequestRepo.save(req));
    }

    @GetMapping("/requests/me")
    public List<LeaveRequest> getMyRequests(Principal principal) {
        Employee emp = getCurrentEmployee(principal);
        return leaveRequestRepo.findByEmployee_EmployeeIdOrderByCreatedAtDesc(emp.getEmployeeId());
    }

    @GetMapping("/team/pending")
    @PreAuthorize("hasAuthority('LEAVE_REVIEW_TEAM')")
    public List<LeaveRequest> getTeamPendingRequests(Principal principal) {
        Employee manager = getCurrentEmployee(principal);
        return leaveRequestRepo.findPendingByManagerId(manager.getEmployeeId());
    }

    @PatchMapping("/requests/{id}/review")
    @PreAuthorize("hasAuthority('LEAVE_REVIEW_TEAM')")
    @Transactional
    public ResponseEntity<?> reviewLeaveRequest(@PathVariable Integer id, @RequestBody Map<String, String> payload, Principal principal) {
        Employee manager = getCurrentEmployee(principal);
        LeaveRequest req = leaveRequestRepo.findById(id).orElseThrow();

        if (req.getEmployee().getManager() == null || !req.getEmployee().getManager().getEmployeeId().equals(manager.getEmployeeId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized to review this leave request.");
        }

        String status = payload.get("status");
        if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) {
            return ResponseEntity.badRequest().body("Invalid status");
        }

        req.setStatus(status);
        req.setReviewedBy(manager);

        if ("APPROVED".equals(status)) {
            LeaveBalance balance = leaveBalanceRepo.findByEmployee_EmployeeIdAndLeaveType_LeaveTypeIdAndBalanceYear(
                req.getEmployee().getEmployeeId(), req.getLeaveType().getLeaveTypeId(), req.getStartDate().getYear()
            ).orElseThrow();

            balance.setUsedDays(balance.getUsedDays().add(req.getTotalDays()));
            leaveBalanceRepo.save(balance);

            // Hook: update attendance records
            for (LocalDate date = req.getStartDate(); !date.isAfter(req.getEndDate()); date = date.plusDays(1)) {
                if (date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                    LocalDate finalDate = date;
                    Optional<AttendanceRecord> optRec = attendanceRepo.findByEmployeeAndAttendanceDate(req.getEmployee(), finalDate);
                    AttendanceRecord rec = optRec.orElseGet(() -> {
                        AttendanceRecord newRec = new AttendanceRecord();
                        newRec.setEmployee(req.getEmployee());
                        newRec.setAttendanceDate(finalDate); 
                        return newRec;
                    });
                    if (optRec.isEmpty()) {
                       rec.setAttendanceDate(finalDate);
                    }
                    rec.setStatus("ON_LEAVE");
                    attendanceRepo.save(rec);
                }
            }
        }
        return ResponseEntity.ok(leaveRequestRepo.save(req));
    }

    @PatchMapping("/requests/{id}/cancel")
    @PreAuthorize("hasAuthority('LEAVE_APPLY')")
    public ResponseEntity<?> cancelLeaveRequest(@PathVariable Integer id, Principal principal) {
        Employee emp = getCurrentEmployee(principal);
        LeaveRequest req = leaveRequestRepo.findById(id).orElseThrow();

        if (!req.getEmployee().getEmployeeId().equals(emp.getEmployeeId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not your request");
        }
        if (!"PENDING".equals(req.getStatus())) {
            return ResponseEntity.badRequest().body("Only PENDING requests can be cancelled");
        }
        
        req.setStatus("CANCELLED");
        return ResponseEntity.ok(leaveRequestRepo.save(req));
    }
}
