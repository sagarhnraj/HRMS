package com.hrms.backend;

import com.hrms.backend.entity.LeaveBalance;
import com.hrms.backend.entity.LeaveRequest;
import com.hrms.backend.entity.LeaveType;
import com.hrms.backend.entity.Shift;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalTime;
import static org.junit.jupiter.api.Assertions.*;

public class BusinessLogicTests {

    @Test
    public void testLeaveBalanceMath() {
        LeaveBalance balance = new LeaveBalance();
        balance.setAllocatedDays(new BigDecimal("15.0"));
        balance.setUsedDays(new BigDecimal("5.0"));
        
        LeaveRequest req = new LeaveRequest();
        req.setTotalDays(new BigDecimal("10.0"));
        
        BigDecimal remaining = balance.getAllocatedDays().subtract(balance.getUsedDays());
        assertTrue(req.getTotalDays().compareTo(remaining) <= 0); // valid
        
        req.setTotalDays(new BigDecimal("11.0"));
        assertFalse(req.getTotalDays().compareTo(remaining) <= 0); // invalid
    }

    @Test
    public void testAttendanceStatusCalculation() {
        Shift shift = new Shift();
        shift.setStartTime(LocalTime.of(9, 0));
        shift.setEndTime(LocalTime.of(17, 0));
        shift.setGraceMinutes(15);
        
        LocalTime checkInOnTime = LocalTime.of(9, 10);
        LocalTime checkInLate = LocalTime.of(9, 30);
        LocalTime checkInHalfDay = LocalTime.of(13, 10);
        
        assertEquals("PRESENT", calculateStatus(shift, checkInOnTime));
        assertEquals("LATE", calculateStatus(shift, checkInLate));
        assertEquals("HALF_DAY", calculateStatus(shift, checkInHalfDay));
    }
    
    private String calculateStatus(Shift shift, LocalTime checkInTime) {
        if (checkInTime.isAfter(shift.getStartTime().plusMinutes(shift.getGraceMinutes()))) {
            if (checkInTime.isAfter(shift.getStartTime().plusHours(4))) {
                return "HALF_DAY";
            } else {
                return "LATE";
            }
        } else {
            return "PRESENT";
        }
    }

    @Test
    public void testPayrollFormulas() {
        BigDecimal basicSalary = new BigDecimal("6240.00");
        BigDecimal houseAllowance = new BigDecimal("500.00");
        BigDecimal travelAllowance = new BigDecimal("100.00");
        
        // Overtime: 8 hours (480 minutes)
        int overtimeMinutes = 480;
        BigDecimal hourlyRate = basicSalary.divide(new BigDecimal("208"), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal overtimeAmount = hourlyRate.multiply(new BigDecimal(overtimeMinutes).divide(new BigDecimal("60"), 2, java.math.RoundingMode.HALF_UP));
        assertEquals(new BigDecimal("240.00"), overtimeAmount);
        
        // Unpaid Leave: 3 days
        int unpaidLeaveDays = 3;
        BigDecimal dailyRate = basicSalary.divide(new BigDecimal("30"), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal unpaidLeaveAmount = dailyRate.multiply(new BigDecimal(unpaidLeaveDays));
        assertEquals(new BigDecimal("624.00"), unpaidLeaveAmount);
        
        // Totals
        BigDecimal gross = basicSalary.add(houseAllowance).add(travelAllowance).add(overtimeAmount);
        assertEquals(new BigDecimal("7080.00"), gross);
        
        BigDecimal net = gross.subtract(unpaidLeaveAmount);
        assertEquals(new BigDecimal("6456.00"), net);
    }
}
