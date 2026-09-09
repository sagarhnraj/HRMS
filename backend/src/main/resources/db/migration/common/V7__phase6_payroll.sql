CREATE TABLE IF NOT EXISTS employee_salaries (
    salary_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL UNIQUE,
    basic_salary DECIMAL(12,2) NOT NULL,
    house_allowance DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    travel_allowance DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    pf_deduction DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    insurance_deduction DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
);

CREATE TABLE IF NOT EXISTS payroll_records (
    payroll_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    payroll_month DATE NOT NULL,
    basic_salary DECIMAL(12,2) NOT NULL,
    allowance_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    incentive_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    overtime_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    tax_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    pf_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    insurance_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    loan_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    unpaid_leave_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    gross_salary DECIMAL(12,2) NOT NULL,
    total_deductions DECIMAL(12,2) NOT NULL,
    net_salary DECIMAL(12,2) NOT NULL,
    status ENUM('DRAFT', 'PROCESSED', 'PAID') NOT NULL DEFAULT 'DRAFT',
    paid_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id),
    UNIQUE (employee_id, payroll_month)
);

-- Insert new permission PAYSLIP_VIEW_SELF if not exists
INSERT INTO permissions (permission_code) 
SELECT 'PAYSLIP_VIEW_SELF' WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE permission_code = 'PAYSLIP_VIEW_SELF'
);

-- Make sure SALARY_MANAGE exists
INSERT INTO permissions (permission_code) 
SELECT 'SALARY_MANAGE' WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE permission_code = 'SALARY_MANAGE'
);

-- Assign permissions
SET @perm_payslip = (SELECT permission_id FROM permissions WHERE permission_code = 'PAYSLIP_VIEW_SELF');
SET @perm_salary = (SELECT permission_id FROM permissions WHERE permission_code = 'SALARY_MANAGE');

SET @role_admin = (SELECT role_id FROM roles WHERE role_name = 'ADMIN');
SET @role_hr = (SELECT role_id FROM roles WHERE role_name = 'HR');
SET @role_emp = (SELECT role_id FROM roles WHERE role_name = 'EMPLOYEE');

-- Admin gets SALARY_MANAGE
INSERT INTO role_permissions (role_id, permission_id) 
SELECT @role_admin, @perm_salary WHERE NOT EXISTS (
    SELECT 1 FROM role_permissions WHERE role_id = @role_admin AND permission_id = @perm_salary
);

-- HR gets SALARY_MANAGE
INSERT INTO role_permissions (role_id, permission_id) 
SELECT @role_hr, @perm_salary WHERE NOT EXISTS (
    SELECT 1 FROM role_permissions WHERE role_id = @role_hr AND permission_id = @perm_salary
);

-- Employee gets PAYSLIP_VIEW_SELF
INSERT INTO role_permissions (role_id, permission_id) 
SELECT @role_emp, @perm_payslip WHERE NOT EXISTS (
    SELECT 1 FROM role_permissions WHERE role_id = @role_emp AND permission_id = @perm_payslip
);
