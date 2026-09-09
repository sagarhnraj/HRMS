package com.hrms.backend.repository;

import com.hrms.backend.entity.EmployeeSalary;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmployeeSalaryRepository extends JpaRepository<EmployeeSalary, Integer> {
    Optional<EmployeeSalary> findByEmployee_EmployeeId(Integer employeeId);
}
