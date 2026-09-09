package com.hrms.backend.controller;

import com.hrms.backend.entity.Employee;
import com.hrms.backend.entity.Timesheet;
import com.hrms.backend.entity.User;
import com.hrms.backend.repository.TimesheetRepository;
import com.hrms.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/timesheets")
public class TimesheetController {

    @Autowired
    private TimesheetRepository timesheetRepo;

    @Autowired
    private UserRepository userRepo;

    private Employee getCurrentEmployee(Principal principal) {
        User user = userRepo.findByEmail(principal.getName()).orElseThrow();
        return user.getEmployee();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('TIMESHEET_SUBMIT')")
    public Timesheet submitTimesheet(@RequestBody Timesheet timesheet, Principal principal) {
        Employee emp = getCurrentEmployee(principal);
        timesheet.setEmployee(emp);
        timesheet.setStatus("SUBMITTED");
        return timesheetRepo.save(timesheet);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('TIMESHEET_SUBMIT')")
    public List<Timesheet> getMyTimesheets(Principal principal) {
        Employee emp = getCurrentEmployee(principal);
        return timesheetRepo.findByEmployee_EmployeeIdOrderByWorkDateDesc(emp.getEmployeeId());
    }

    @GetMapping("/team/pending")
    @PreAuthorize("hasAuthority('TIMESHEET_REVIEW_TEAM')")
    public List<Timesheet> getTeamPendingTimesheets(Principal principal) {
        Employee manager = getCurrentEmployee(principal);
        return timesheetRepo.findPendingByManagerId(manager.getEmployeeId());
    }

    @PatchMapping("/{id}/review")
    @PreAuthorize("hasAuthority('TIMESHEET_REVIEW_TEAM')")
    public ResponseEntity<?> reviewTimesheet(@PathVariable Integer id, @RequestBody Map<String, String> payload, Principal principal) {
        Employee manager = getCurrentEmployee(principal);
        Timesheet timesheet = timesheetRepo.findById(id).orElseThrow();

        if (timesheet.getEmployee().getManager() == null || !timesheet.getEmployee().getManager().getEmployeeId().equals(manager.getEmployeeId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized to review this timesheet.");
        }

        String status = payload.get("status");
        if (status == null || (!status.equals("APPROVED") && !status.equals("REJECTED"))) {
            return ResponseEntity.badRequest().body("Invalid status.");
        }

        timesheet.setStatus(status);
        timesheet.setReviewedBy(manager);
        return ResponseEntity.ok(timesheetRepo.save(timesheet));
    }
}
