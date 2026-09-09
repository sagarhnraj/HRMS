package com.hrms.backend.controller;

import com.hrms.backend.entity.Employee;
import com.hrms.backend.entity.User;
import com.hrms.backend.repository.EmployeeRepository;
import com.hrms.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.hrms.backend.repository.LeaveTypeRepository;
import com.hrms.backend.repository.LeaveBalanceRepository;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;

    public EmployeeController(EmployeeRepository employeeRepository, UserRepository userRepository,
                              LeaveTypeRepository leaveTypeRepository, LeaveBalanceRepository leaveBalanceRepository) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.leaveTypeRepository = leaveTypeRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('EMPLOYEE_CREATE') or hasAuthority('EMPLOYEE_UPDATE')")
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('PROFILE_VIEW_SELF')")
    public ResponseEntity<Employee> getMyProfile(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        Optional<User> userOptional = userRepository.findByEmail(principal.getName());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return ResponseEntity.ok(user.getEmployee());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('EMPLOYEE_CREATE')")
    public Employee createEmployee(@RequestBody Employee employee) {
        employee.setCreatedAt(java.time.LocalDateTime.now());
        employee.setUpdatedAt(java.time.LocalDateTime.now());
        Employee saved = employeeRepository.save(employee);
        
        // Initialize Leave Balances for the current year
        int currentYear = java.time.LocalDate.now().getYear();
        List<com.hrms.backend.entity.LeaveType> leaveTypes = leaveTypeRepository.findAll();
        for (com.hrms.backend.entity.LeaveType lt : leaveTypes) {
            com.hrms.backend.entity.LeaveBalance bal = new com.hrms.backend.entity.LeaveBalance();
            bal.setEmployee(saved);
            bal.setLeaveType(lt);
            bal.setBalanceYear(currentYear);
            bal.setAllocatedDays(lt.getDaysPerYear());
            bal.setUsedDays(java.math.BigDecimal.ZERO);
            leaveBalanceRepository.save(bal);
        }
        
        return saved;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE_UPDATE')")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Integer id, @RequestBody Employee employeeDetails) {
        return employeeRepository.findById(id).map(employee -> {
            employee.setFirstName(employeeDetails.getFirstName());
            employee.setLastName(employeeDetails.getLastName());
            employee.setPhone(employeeDetails.getPhone());
            employee.setDepartment(employeeDetails.getDepartment());
            employee.setDesignation(employeeDetails.getDesignation());
            employee.setLocation(employeeDetails.getLocation());
            employee.setShift(employeeDetails.getShift());
            employee.setManager(employeeDetails.getManager());
            employee.setEmploymentType(employeeDetails.getEmploymentType());
            employee.setJoiningDate(employeeDetails.getJoiningDate());
            employee.setUpdatedAt(java.time.LocalDateTime.now());
            return ResponseEntity.ok(employeeRepository.save(employee));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('EMPLOYEE_UPDATE')")
    public ResponseEntity<Employee> updateEmployeeStatus(@PathVariable Integer id, @RequestBody Map<String, String> statusUpdate) {
        return employeeRepository.findById(id).map(employee -> {
            if (statusUpdate.containsKey("status")) {
                employee.setStatus(statusUpdate.get("status"));
                employee.setUpdatedAt(java.time.LocalDateTime.now());
                return ResponseEntity.ok(employeeRepository.save(employee));
            }
            return ResponseEntity.badRequest().body(employee);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/me")
    @PreAuthorize("hasAuthority('PROFILE_UPDATE_SELF')")
    public ResponseEntity<Employee> updateMyProfile(Principal principal, @RequestBody Map<String, String> profileUpdate) {
        if (principal == null) return ResponseEntity.status(401).build();
        Optional<User> userOptional = userRepository.findByEmail(principal.getName());
        if (userOptional.isPresent()) {
            Employee employee = userOptional.get().getEmployee();
            if (employee != null) {
                if (profileUpdate.containsKey("phone")) {
                    employee.setPhone(profileUpdate.get("phone"));
                    employee.setUpdatedAt(java.time.LocalDateTime.now());
                }
                return ResponseEntity.ok(employeeRepository.save(employee));
            }
        }
        return ResponseEntity.notFound().build();
    }
}
