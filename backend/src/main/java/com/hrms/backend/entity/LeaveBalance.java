package com.hrms.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_balances")
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer balanceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private LeaveType leaveType;

    @Column(nullable = false)
    private Integer balanceYear;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal allocatedDays;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal usedDays = BigDecimal.ZERO;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Transient
    public BigDecimal getRemainingDays() {
        if (allocatedDays != null && usedDays != null) {
            return allocatedDays.subtract(usedDays);
        }
        return BigDecimal.ZERO;
    }

    public Integer getBalanceId() { return balanceId; }
    public void setBalanceId(Integer balanceId) { this.balanceId = balanceId; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public LeaveType getLeaveType() { return leaveType; }
    public void setLeaveType(LeaveType leaveType) { this.leaveType = leaveType; }

    public Integer getBalanceYear() { return balanceYear; }
    public void setBalanceYear(Integer balanceYear) { this.balanceYear = balanceYear; }

    public BigDecimal getAllocatedDays() { return allocatedDays; }
    public void setAllocatedDays(BigDecimal allocatedDays) { this.allocatedDays = allocatedDays; }

    public BigDecimal getUsedDays() { return usedDays; }
    public void setUsedDays(BigDecimal usedDays) { this.usedDays = usedDays; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
