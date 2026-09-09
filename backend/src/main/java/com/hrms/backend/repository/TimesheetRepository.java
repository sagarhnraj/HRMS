package com.hrms.backend.repository;

import com.hrms.backend.entity.Timesheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TimesheetRepository extends JpaRepository<Timesheet, Integer> {
    List<Timesheet> findByEmployee_EmployeeIdOrderByWorkDateDesc(Integer employeeId);

    @Query("SELECT t FROM Timesheet t WHERE t.employee.manager.employeeId = :managerId AND t.status = 'SUBMITTED'")
    List<Timesheet> findPendingByManagerId(@Param("managerId") Integer managerId);
}
