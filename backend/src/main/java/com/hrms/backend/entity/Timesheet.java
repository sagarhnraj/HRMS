package com.hrms.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "timesheets")
public class Timesheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer timesheetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate workDate;

    @Column(nullable = false, length = 500)
    private String taskDescription;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal hoursWorked;

    @Column(nullable = false)
    private String status = "SUBMITTED";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private Employee reviewedBy;

    // Getters and Setters
    public Integer getTimesheetId() { return timesheetId; }
    public void setTimesheetId(Integer timesheetId) { this.timesheetId = timesheetId; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public LocalDate getWorkDate() { return workDate; }
    public void setWorkDate(LocalDate workDate) { this.workDate = workDate; }

    public String getTaskDescription() { return taskDescription; }
    public void setTaskDescription(String taskDescription) { this.taskDescription = taskDescription; }

    public BigDecimal getHoursWorked() { return hoursWorked; }
    public void setHoursWorked(BigDecimal hoursWorked) { this.hoursWorked = hoursWorked; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Employee getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(Employee reviewedBy) { this.reviewedBy = reviewedBy; }
}
