package com.hrms.backend.controller;

import com.hrms.backend.entity.Department;
import com.hrms.backend.entity.Designation;
import com.hrms.backend.entity.OfficeLocation;
import com.hrms.backend.entity.Shift;
import com.hrms.backend.repository.DepartmentRepository;
import com.hrms.backend.repository.DesignationRepository;
import com.hrms.backend.repository.OfficeLocationRepository;
import com.hrms.backend.repository.ShiftRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RestController
@RequestMapping("/api/org")
@PreAuthorize("hasAuthority('EMPLOYEE_CREATE') or hasAuthority('ROLE_MANAGE') or hasAuthority('SHIFT_MANAGE')")
public class OrgController {

    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final OfficeLocationRepository officeLocationRepository;
    private final ShiftRepository shiftRepository;

    public OrgController(DepartmentRepository departmentRepository,
                         DesignationRepository designationRepository,
                         OfficeLocationRepository officeLocationRepository,
                         ShiftRepository shiftRepository) {
        this.departmentRepository = departmentRepository;
        this.designationRepository = designationRepository;
        this.officeLocationRepository = officeLocationRepository;
        this.shiftRepository = shiftRepository;
    }

    // Departments
    @PostMapping("/departments")
    public Department createDepartment(@RequestBody Department dept) {
        return departmentRepository.save(dept);
    }
    @PutMapping("/departments/{id}")
    public Department updateDepartment(@PathVariable Integer id, @RequestBody Department dept) {
        Department existing = departmentRepository.findById(id).orElseThrow();
        existing.setDepartmentName(dept.getDepartmentName());
        return departmentRepository.save(existing);
    }
    @DeleteMapping("/departments/{id}")
    public void deleteDepartment(@PathVariable Integer id) {
        departmentRepository.deleteById(id);
    }

    // Designations
    @PostMapping("/designations")
    public Designation createDesignation(@RequestBody Designation desig) {
        return designationRepository.save(desig);
    }
    @PutMapping("/designations/{id}")
    public Designation updateDesignation(@PathVariable Integer id, @RequestBody Designation desig) {
        Designation existing = designationRepository.findById(id).orElseThrow();
        existing.setDesignationName(desig.getDesignationName());
        return designationRepository.save(existing);
    }
    @DeleteMapping("/designations/{id}")
    public void deleteDesignation(@PathVariable Integer id) {
        designationRepository.deleteById(id);
    }

    // Locations
    @PostMapping("/locations")
    public OfficeLocation createLocation(@RequestBody OfficeLocation loc) {
        return officeLocationRepository.save(loc);
    }
    @PutMapping("/locations/{id}")
    public OfficeLocation updateLocation(@PathVariable Integer id, @RequestBody OfficeLocation loc) {
        OfficeLocation existing = officeLocationRepository.findById(id).orElseThrow();
        existing.setLocationName(loc.getLocationName());
        existing.setAddress(loc.getAddress());
        return officeLocationRepository.save(existing);
    }
    @DeleteMapping("/locations/{id}")
    public void deleteLocation(@PathVariable Integer id) {
        officeLocationRepository.deleteById(id);
    }

    // Shifts
    @PostMapping("/shifts")
    @PreAuthorize("hasAuthority('SHIFT_MANAGE')")
    public Shift createShift(@RequestBody Shift shift) {
        return shiftRepository.save(shift);
    }
    @PutMapping("/shifts/{id}")
    @PreAuthorize("hasAuthority('SHIFT_MANAGE')")
    public Shift updateShift(@PathVariable Integer id, @RequestBody Shift shift) {
        Shift existing = shiftRepository.findById(id).orElseThrow();
        existing.setShiftName(shift.getShiftName());
        existing.setStartTime(shift.getStartTime());
        existing.setEndTime(shift.getEndTime());
        existing.setGraceMinutes(shift.getGraceMinutes());
        return shiftRepository.save(existing);
    }
    @DeleteMapping("/shifts/{id}")
    @PreAuthorize("hasAuthority('SHIFT_MANAGE')")
    public void deleteShift(@PathVariable Integer id) {
        shiftRepository.deleteById(id);
    }
}
