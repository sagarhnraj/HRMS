package com.hrms.backend.repository;

import com.hrms.backend.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Integer> {
    List<LeaveRequest> findByEmployee_EmployeeIdOrderByCreatedAtDesc(Integer employeeId);

    @Query("SELECT r FROM LeaveRequest r WHERE r.employee.manager.employeeId = :managerId AND r.status = 'PENDING' ORDER BY r.createdAt DESC")
    List<LeaveRequest> findPendingByManagerId(@Param("managerId") Integer managerId);

    @Query("SELECT r FROM LeaveRequest r WHERE r.employee.employeeId = :employeeId AND r.status = 'APPROVED' AND r.leaveType.isPaid = false AND r.startDate <= :monthEnd AND r.endDate >= :monthStart")
    List<LeaveRequest> findApprovedUnpaidLeaves(@Param("employeeId") Integer employeeId, @Param("monthStart") java.time.LocalDate monthStart, @Param("monthEnd") java.time.LocalDate monthEnd);
}
