package com.hrms.backend.repository;
import com.hrms.backend.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
public interface DepartmentRepository extends JpaRepository<Department, Integer> {}
