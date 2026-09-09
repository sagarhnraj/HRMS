package com.hrms.backend.repository;

import com.hrms.backend.entity.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Integer> {
    List<LeaveBalance> findByEmployee_EmployeeIdAndBalanceYear(Integer employeeId, Integer balanceYear);
    Optional<LeaveBalance> findByEmployee_EmployeeIdAndLeaveType_LeaveTypeIdAndBalanceYear(Integer employeeId, Integer leaveTypeId, Integer balanceYear);
}
