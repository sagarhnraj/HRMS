package com.hrms.backend.controller;

import com.hrms.backend.entity.*;
import com.hrms.backend.repository.*;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payroll")
public class PayrollController {

    @Autowired private EmployeeSalaryRepository employeeSalaryRepo;
    @Autowired private PayrollRecordRepository payrollRepo;
    @Autowired private EmployeeRepository employeeRepo;
    @Autowired private AttendanceRecordRepository attendanceRepo;
    @Autowired private LeaveRequestRepository leaveRepo;
    @Autowired private UserRepository userRepo;

    private Employee getCurrentEmployee(Principal principal) {
        User user = userRepo.findByEmail(principal.getName()).orElseThrow();
        return user.getEmployee();
    }

    // --- SALARY STRUCTURE ---
    @GetMapping("/salary/{employeeId}")
    @PreAuthorize("hasAuthority('SALARY_MANAGE')")
    public ResponseEntity<EmployeeSalary> getSalary(@PathVariable Integer employeeId) {
        return employeeSalaryRepo.findByEmployee_EmployeeId(employeeId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/salary/{employeeId}")
    @PreAuthorize("hasAuthority('SALARY_MANAGE')")
    public EmployeeSalary setSalary(@PathVariable Integer employeeId, @RequestBody EmployeeSalary salary) {
        Employee emp = employeeRepo.findById(employeeId).orElseThrow();
        EmployeeSalary existing = employeeSalaryRepo.findByEmployee_EmployeeId(employeeId).orElse(new EmployeeSalary());
        existing.setEmployee(emp);
        existing.setBasicSalary(salary.getBasicSalary());
        existing.setHouseAllowance(salary.getHouseAllowance() != null ? salary.getHouseAllowance() : BigDecimal.ZERO);
        existing.setTravelAllowance(salary.getTravelAllowance() != null ? salary.getTravelAllowance() : BigDecimal.ZERO);
        existing.setPfDeduction(salary.getPfDeduction() != null ? salary.getPfDeduction() : BigDecimal.ZERO);
        existing.setInsuranceDeduction(salary.getInsuranceDeduction() != null ? salary.getInsuranceDeduction() : BigDecimal.ZERO);
        return employeeSalaryRepo.save(existing);
    }

    // --- PAYROLL ENGINE ---
    @PostMapping("/generate")
    @PreAuthorize("hasAuthority('SALARY_MANAGE')")
    @Transactional
    public ResponseEntity<?> generatePayroll(@RequestParam String month) {
        // month is YYYY-MM
        LocalDate payrollMonth = LocalDate.parse(month + "-01");
        LocalDate monthEnd = payrollMonth.withDayOfMonth(payrollMonth.lengthOfMonth());

        List<Employee> activeEmployees = employeeRepo.findAll().stream()
            .filter(e -> "ACTIVE".equals(e.getStatus()))
            .toList();
        
        int createdCount = 0;

        for (Employee emp : activeEmployees) {
            Optional<PayrollRecord> existing = payrollRepo.findByEmployee_EmployeeIdAndPayrollMonth(emp.getEmployeeId(), payrollMonth);
            if (existing.isPresent()) continue; // Idempotent

            Optional<EmployeeSalary> optSalary = employeeSalaryRepo.findByEmployee_EmployeeId(emp.getEmployeeId());
            if (optSalary.isEmpty()) continue; // No salary structure defined

            EmployeeSalary sal = optSalary.get();

            PayrollRecord rec = new PayrollRecord();
            rec.setEmployee(emp);
            rec.setPayrollMonth(payrollMonth);
            rec.setBasicSalary(sal.getBasicSalary());
            rec.setAllowanceAmount(sal.getHouseAllowance().add(sal.getTravelAllowance()));
            rec.setPfAmount(sal.getPfDeduction());
            rec.setInsuranceAmount(sal.getInsuranceDeduction());

            // Overtime amount
            Integer otMinutes = attendanceRepo.sumOvertimeMinutes(emp.getEmployeeId(), payrollMonth, monthEnd);
            if (otMinutes == null) otMinutes = 0;
            BigDecimal hourlyRate = sal.getBasicSalary().divide(new BigDecimal("208"), 2, RoundingMode.HALF_UP);
            BigDecimal otHours = new BigDecimal(otMinutes).divide(new BigDecimal("60"), 2, RoundingMode.HALF_UP);
            rec.setOvertimeAmount(hourlyRate.multiply(otHours));

            // Unpaid leave amount
            List<LeaveRequest> unpaidLeaves = leaveRepo.findApprovedUnpaidLeaves(emp.getEmployeeId(), payrollMonth, monthEnd);
            long totalUnpaidDays = 0;
            for (LeaveRequest lr : unpaidLeaves) {
                LocalDate overlapStart = lr.getStartDate().isBefore(payrollMonth) ? payrollMonth : lr.getStartDate();
                LocalDate overlapEnd = lr.getEndDate().isAfter(monthEnd) ? monthEnd : lr.getEndDate();
                long overlap = ChronoUnit.DAYS.between(overlapStart, overlapEnd) + 1;
                if (overlap > 0) totalUnpaidDays += overlap;
            }
            BigDecimal dailyRate = sal.getBasicSalary().divide(new BigDecimal("30"), 2, RoundingMode.HALF_UP);
            rec.setUnpaidLeaveAmount(dailyRate.multiply(new BigDecimal(totalUnpaidDays)));

            rec.recalculateTotals();
            payrollRepo.save(rec);
            createdCount++;
        }

        return ResponseEntity.ok("Generated " + createdCount + " payroll records for " + month);
    }

    @GetMapping("/month")
    @PreAuthorize("hasAuthority('SALARY_MANAGE')")
    public List<PayrollRecord> getPayrollForMonth(@RequestParam String month) {
        LocalDate payrollMonth = LocalDate.parse(month + "-01");
        return payrollRepo.findByPayrollMonth(payrollMonth);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('SALARY_MANAGE')")
    public ResponseEntity<?> updatePayroll(@PathVariable Integer id, @RequestBody Map<String, BigDecimal> amounts) {
        PayrollRecord rec = payrollRepo.findById(id).orElseThrow();
        if (!"DRAFT".equals(rec.getStatus())) {
            return ResponseEntity.badRequest().body("Can only edit DRAFT records");
        }
        if (amounts.containsKey("incentiveAmount")) rec.setIncentiveAmount(amounts.get("incentiveAmount"));
        if (amounts.containsKey("taxAmount")) rec.setTaxAmount(amounts.get("taxAmount"));
        if (amounts.containsKey("loanAmount")) rec.setLoanAmount(amounts.get("loanAmount"));
        
        rec.recalculateTotals();
        return ResponseEntity.ok(payrollRepo.save(rec));
    }

    @PatchMapping("/{id}/process")
    @PreAuthorize("hasAuthority('SALARY_MANAGE')")
    public ResponseEntity<?> processPayroll(@PathVariable Integer id) {
        PayrollRecord rec = payrollRepo.findById(id).orElseThrow();
        if (!"DRAFT".equals(rec.getStatus())) return ResponseEntity.badRequest().body("Must be DRAFT to process");
        rec.setStatus("PROCESSED");
        return ResponseEntity.ok(payrollRepo.save(rec));
    }

    @PatchMapping("/{id}/pay")
    @PreAuthorize("hasAuthority('SALARY_MANAGE')")
    public ResponseEntity<?> payPayroll(@PathVariable Integer id) {
        PayrollRecord rec = payrollRepo.findById(id).orElseThrow();
        if (!"PROCESSED".equals(rec.getStatus())) return ResponseEntity.badRequest().body("Must be PROCESSED to pay");
        rec.setStatus("PAID");
        rec.setPaidAt(LocalDateTime.now());
        return ResponseEntity.ok(payrollRepo.save(rec));
    }

    // --- PAYSLIP ACCESS ---
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('PAYSLIP_VIEW_SELF')")
    public List<PayrollRecord> getMyPayslips(Principal principal) {
        Employee emp = getCurrentEmployee(principal);
        return payrollRepo.findByEmployee_EmployeeId(emp.getEmployeeId()).stream()
            .filter(r -> "PROCESSED".equals(r.getStatus()) || "PAID".equals(r.getStatus()))
            .toList();
    }

    private String formatINR(BigDecimal amount) {
        java.text.NumberFormat numFormat = java.text.NumberFormat.getNumberInstance(new java.util.Locale("en", "IN"));
        numFormat.setMinimumFractionDigits(2);
        numFormat.setMaximumFractionDigits(2);
        return "INR " + numFormat.format(amount != null ? amount : BigDecimal.ZERO);
    }

    @GetMapping("/{id}/payslip")
    @PreAuthorize("hasAuthority('PAYSLIP_VIEW_SELF')")
    public ResponseEntity<byte[]> downloadPayslip(@PathVariable Integer id, Principal principal) {
        Employee emp = getCurrentEmployee(principal);
        PayrollRecord rec = payrollRepo.findById(id).orElseThrow();
        
        if (!rec.getEmployee().getEmployeeId().equals(emp.getEmployeeId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if ("DRAFT".equals(rec.getStatus())) {
            return ResponseEntity.badRequest().build();
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();
            
            document.add(new Paragraph("ACME Corp - Official Payslip"));
            document.add(new Paragraph("Employee: " + emp.getFirstName() + " " + emp.getLastName() + " (" + emp.getEmployeeCode() + ")"));
            document.add(new Paragraph("Month: " + rec.getPayrollMonth().format(DateTimeFormatter.ofPattern("MMM yyyy"))));
            document.add(new Paragraph("Status: " + rec.getStatus()));
            document.add(new Paragraph("\n"));
            
            document.add(new Paragraph("Earnings:"));
            document.add(new Paragraph("Basic Salary: " + formatINR(rec.getBasicSalary())));
            document.add(new Paragraph("Allowances: " + formatINR(rec.getAllowanceAmount())));
            document.add(new Paragraph("Incentives: " + formatINR(rec.getIncentiveAmount())));
            document.add(new Paragraph("Overtime: " + formatINR(rec.getOvertimeAmount())));
            document.add(new Paragraph("Gross Salary: " + formatINR(rec.getGrossSalary())));
            document.add(new Paragraph("\n"));
            
            document.add(new Paragraph("Deductions:"));
            document.add(new Paragraph("Tax: " + formatINR(rec.getTaxAmount())));
            document.add(new Paragraph("PF: " + formatINR(rec.getPfAmount())));
            document.add(new Paragraph("Insurance: " + formatINR(rec.getInsuranceAmount())));
            document.add(new Paragraph("Loan: " + formatINR(rec.getLoanAmount())));
            document.add(new Paragraph("Unpaid Leave: " + formatINR(rec.getUnpaidLeaveAmount())));
            document.add(new Paragraph("Total Deductions: " + formatINR(rec.getTotalDeductions())));
            document.add(new Paragraph("\n"));
            
            document.add(new Paragraph("NET SALARY: " + formatINR(rec.getNetSalary())));
            document.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "payslip_" + rec.getPayrollMonth().toString() + ".pdf");
            return new ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
