package com.hrms.backend.scheduler;

import com.hrms.backend.entity.AttendanceRecord;
import com.hrms.backend.entity.Employee;
import com.hrms.backend.repository.AttendanceRecordRepository;
import com.hrms.backend.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.List;

@Component
public class AttendanceScheduler {

    @Autowired
    private EmployeeRepository employeeRepo;

    @Autowired
    private AttendanceRecordRepository attendanceRepo;

    @Scheduled(cron = "0 0 1 * * MON-FRI") // Every weekday at 1:00 AM
    public void markAbsentForPreviousWorkingDay() {
        LocalDate today = LocalDate.now();
        LocalDate targetDate = today.minusDays(1);
        if (today.getDayOfWeek() == DayOfWeek.MONDAY) {
            targetDate = today.minusDays(3); // Friday
        }

        List<Employee> activeEmployees = employeeRepo.findAll();
        for (Employee emp : activeEmployees) {
            if ("ACTIVE".equals(emp.getStatus())) {
                boolean hasRecord = attendanceRepo.findByEmployeeAndAttendanceDate(emp, targetDate).isPresent();
                if (!hasRecord) {
                    AttendanceRecord absentRecord = new AttendanceRecord();
                    absentRecord.setEmployee(emp);
                    absentRecord.setAttendanceDate(targetDate);
                    absentRecord.setStatus("ABSENT");
                    attendanceRepo.save(absentRecord);
                }
            }
        }
    }
}
