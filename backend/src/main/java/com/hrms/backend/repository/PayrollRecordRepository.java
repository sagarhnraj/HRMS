package com.hrms.backend.repository;

import com.hrms.backend.entity.PayrollRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PayrollRecordRepository extends JpaRepository<PayrollRecord, Integer> {
    Optional<PayrollRecord> findByEmployee_EmployeeIdAndPayrollMonth(Integer employeeId, LocalDate payrollMonth);
    List<PayrollRecord> findByPayrollMonth(LocalDate payrollMonth);
    List<PayrollRecord> findByEmployee_EmployeeId(Integer employeeId);
}
