package com.hrms.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.hrms.backend.entity.*;
import com.hrms.backend.repository.*;
import com.hrms.backend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class IntegrationTests {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private TimesheetRepository timesheetRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private DesignationRepository designationRepository;
    
    @Autowired
    private OfficeLocationRepository locationRepository;
    
    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @BeforeEach
    public void setup() {
        timesheetRepository.deleteAll();
        userRepository.deleteAll();
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();
        designationRepository.deleteAll();
        locationRepository.deleteAll();
        shiftRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();
        
        Department dept = departmentRepository.save(new Department("Engineering"));
        Designation desig = designationRepository.save(new Designation("Developer", new BigDecimal("50000")));
        OfficeLocation loc = locationRepository.save(new OfficeLocation("HQ", "123 Main St"));
        Shift shift = shiftRepository.save(new Shift("Day", LocalTime.of(9, 0), LocalTime.of(17, 0), 15));
        
        Role empRole = roleRepository.save(new Role("EMPLOYEE"));
        Role mgrRole = roleRepository.save(new Role("MANAGER"));
        
        Permission teamReview = permissionRepository.save(new Permission("LEAVE_REVIEW_TEAM"));
        mgrRole.getPermissions().add(teamReview);
        roleRepository.save(mgrRole);

        // Manager 1
        Employee m1 = new Employee("MGR01", "Alice", "Manager1", "ACTIVE", dept, desig, loc, shift);
        m1 = employeeRepository.save(m1);
        User uM1 = new User(m1, "m1@test.com", "pass", "ACTIVE");
        uM1.getRoles().addAll(List.of(empRole, mgrRole));
        userRepository.save(uM1);

        // Manager 2
        Employee m2 = new Employee("MGR02", "Bob", "Manager2", "ACTIVE", dept, desig, loc, shift);
        m2 = employeeRepository.save(m2);
        User uM2 = new User(m2, "m2@test.com", "pass", "ACTIVE");
        uM2.getRoles().addAll(List.of(empRole, mgrRole));
        userRepository.save(uM2);

        // Employee under Manager 1
        Employee e1 = new Employee("EMP01", "Charlie", "Worker", "ACTIVE", dept, desig, loc, shift);
        e1.setManager(m1);
        e1 = employeeRepository.save(e1);
        User uE1 = new User(e1, "e1@test.com", "pass", "ACTIVE");
        uE1.getRoles().add(empRole);
        userRepository.save(uE1);

        Timesheet ts = new Timesheet(e1, LocalDate.now(), LocalDate.now().plusDays(6), 40, "PENDING", null);
        timesheetRepository.save(ts);
    }

    @Test
    public void testUnauthorizedAccessToProtectedEndpoint() throws Exception {
        mockMvc.perform(get("/api/employees/me"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    public void testInvalidTokenRejected() throws Exception {
        mockMvc.perform(get("/api/employees/me")
               .header("Authorization", "Bearer invalidtoken123"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    public void testValidationFailureReturns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
               .contentType("application/json")
               .content("{\"email\": \"invalid-email\", \"password\": \"\"}"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    public void testTimesheetBypassRejected() throws Exception {
        // Find timesheet
        Timesheet ts = timesheetRepository.findAll().get(0);
        
        // Login as Manager 2
        String m2Token = jwtUtil.generateToken("m2@test.com");
        
        // M2 tries to review E1's timesheet (E1 is managed by M1)
        mockMvc.perform(patch("/api/timesheets/" + ts.getTimesheetId() + "/review")
               .header("Authorization", "Bearer " + m2Token)
               .contentType("application/json")
               .content("{\"status\": \"APPROVED\"}"))
               .andExpect(status().isForbidden());
    }
    
    @Test
    public void testTimesheetReviewAllowedForDirectManager() throws Exception {
        Timesheet ts = timesheetRepository.findAll().get(0);
        
        String m1Token = jwtUtil.generateToken("m1@test.com");
        
        mockMvc.perform(patch("/api/timesheets/" + ts.getTimesheetId() + "/review")
               .header("Authorization", "Bearer " + m1Token)
               .contentType("application/json")
               .content("{\"status\": \"APPROVED\"}"))
               .andExpect(status().isOk());
    }
}
