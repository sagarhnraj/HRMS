package com.hrms.backend.repository;

import com.hrms.backend.entity.AttendanceRecord;
import com.hrms.backend.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Integer> {
    Optional<AttendanceRecord> findByEmployeeAndAttendanceDate(Employee employee, LocalDate attendanceDate);

    @Query("SELECT a FROM AttendanceRecord a WHERE a.employee.employeeId = :employeeId AND FUNCTION('DATE_FORMAT', a.attendanceDate, '%Y-%m') = :monthStr")
    List<AttendanceRecord> findByEmployeeIdAndMonth(@Param("employeeId") Integer employeeId, @Param("monthStr") String monthStr);

    @Query("SELECT a FROM AttendanceRecord a WHERE a.employee.manager.employeeId = :managerId AND a.attendanceDate = :date")
    List<AttendanceRecord> findByManagerIdAndDate(@Param("managerId") Integer managerId, @Param("date") LocalDate date);

    @Query("SELECT SUM(a.overtimeMinutes) FROM AttendanceRecord a WHERE a.employee.employeeId = :employeeId AND a.attendanceDate >= :startDate AND a.attendanceDate <= :endDate")
    Integer sumOvertimeMinutes(@Param("employeeId") Integer employeeId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
