package com.hrms.backend.controller;

import com.hrms.backend.entity.Department;
import com.hrms.backend.entity.Designation;
import com.hrms.backend.entity.OfficeLocation;
import com.hrms.backend.entity.Shift;
import com.hrms.backend.repository.DepartmentRepository;
import com.hrms.backend.repository.DesignationRepository;
import com.hrms.backend.repository.OfficeLocationRepository;
import com.hrms.backend.repository.ShiftRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lookups")
public class LookupController {

    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final OfficeLocationRepository officeLocationRepository;
    private final ShiftRepository shiftRepository;

    public LookupController(DepartmentRepository departmentRepository,
                            DesignationRepository designationRepository,
                            OfficeLocationRepository officeLocationRepository,
                            ShiftRepository shiftRepository) {
        this.departmentRepository = departmentRepository;
        this.designationRepository = designationRepository;
        this.officeLocationRepository = officeLocationRepository;
        this.shiftRepository = shiftRepository;
    }

    @GetMapping("/departments")
    public List<Department> getDepartments() {
        return departmentRepository.findAll();
    }

    @GetMapping("/designations")
    public List<Designation> getDesignations() {
        return designationRepository.findAll();
    }

    @GetMapping("/locations")
    public List<OfficeLocation> getLocations() {
        return officeLocationRepository.findAll();
    }

    @GetMapping("/shifts")
    public List<Shift> getShifts() {
        return shiftRepository.findAll();
    }
}
