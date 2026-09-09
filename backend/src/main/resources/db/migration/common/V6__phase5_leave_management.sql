CREATE TABLE IF NOT EXISTS leave_types (
    leave_type_id INT AUTO_INCREMENT PRIMARY KEY,
    leave_name VARCHAR(100) NOT NULL UNIQUE,
    days_per_year DECIMAL(5,2) NOT NULL,
    is_paid BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS leave_balances (
    balance_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    leave_type_id INT NOT NULL,
    balance_year INT NOT NULL,
    allocated_days DECIMAL(5,2) NOT NULL,
    used_days DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id),
    FOREIGN KEY (leave_type_id) REFERENCES leave_types(leave_type_id),
    UNIQUE (employee_id, leave_type_id, balance_year)
);

CREATE TABLE IF NOT EXISTS leave_requests (
    leave_request_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    leave_type_id INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_days DECIMAL(5,2) NOT NULL,
    reason VARCHAR(500),
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    reviewed_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id),
    FOREIGN KEY (leave_type_id) REFERENCES leave_types(leave_type_id),
    FOREIGN KEY (reviewed_by) REFERENCES employees(employee_id)
);

-- Insert new permission LEAVE_POLICY_MANAGE if not exists
INSERT INTO permissions (permission_code) 
SELECT 'LEAVE_POLICY_MANAGE' WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE permission_code = 'LEAVE_POLICY_MANAGE'
);

-- Make sure LEAVE_APPLY and LEAVE_REVIEW_TEAM exist (they were supposed to be in Phase 1)
INSERT INTO permissions (permission_code) 
SELECT 'LEAVE_APPLY' WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE permission_code = 'LEAVE_APPLY'
);
INSERT INTO permissions (permission_code) 
SELECT 'LEAVE_REVIEW_TEAM' WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE permission_code = 'LEAVE_REVIEW_TEAM'
);

-- Assign permissions
SET @perm_policy = (SELECT permission_id FROM permissions WHERE permission_code = 'LEAVE_POLICY_MANAGE');
SET @perm_apply = (SELECT permission_id FROM permissions WHERE permission_code = 'LEAVE_APPLY');
SET @perm_review = (SELECT permission_id FROM permissions WHERE permission_code = 'LEAVE_REVIEW_TEAM');

SET @role_admin = (SELECT role_id FROM roles WHERE role_name = 'ADMIN');
SET @role_hr = (SELECT role_id FROM roles WHERE role_name = 'HR');
SET @role_mgr = (SELECT role_id FROM roles WHERE role_name = 'MANAGER');
SET @role_emp = (SELECT role_id FROM roles WHERE role_name = 'EMPLOYEE');

-- Admin gets LEAVE_POLICY_MANAGE
INSERT INTO role_permissions (role_id, permission_id) 
SELECT @role_admin, @perm_policy WHERE NOT EXISTS (
    SELECT 1 FROM role_permissions WHERE role_id = @role_admin AND permission_id = @perm_policy
);

-- HR gets LEAVE_POLICY_MANAGE
INSERT INTO role_permissions (role_id, permission_id) 
SELECT @role_hr, @perm_policy WHERE NOT EXISTS (
    SELECT 1 FROM role_permissions WHERE role_id = @role_hr AND permission_id = @perm_policy
);

-- Employee gets LEAVE_APPLY
INSERT INTO role_permissions (role_id, permission_id) 
SELECT @role_emp, @perm_apply WHERE NOT EXISTS (
    SELECT 1 FROM role_permissions WHERE role_id = @role_emp AND permission_id = @perm_apply
);

-- Manager gets LEAVE_APPLY (if not already via EMPLOYEE dual role)
INSERT INTO role_permissions (role_id, permission_id) 
SELECT @role_mgr, @perm_apply WHERE NOT EXISTS (
    SELECT 1 FROM role_permissions WHERE role_id = @role_mgr AND permission_id = @perm_apply
);

-- Manager gets LEAVE_REVIEW_TEAM
INSERT INTO role_permissions (role_id, permission_id) 
SELECT @role_mgr, @perm_review WHERE NOT EXISTS (
    SELECT 1 FROM role_permissions WHERE role_id = @role_mgr AND permission_id = @perm_review
);

-- Seed Leave Types
INSERT INTO leave_types (leave_name, days_per_year, is_paid)
SELECT 'Casual Leave', 12.00, TRUE WHERE NOT EXISTS (SELECT 1 FROM leave_types WHERE leave_name = 'Casual Leave');

INSERT INTO leave_types (leave_name, days_per_year, is_paid)
SELECT 'Sick Leave', 10.00, TRUE WHERE NOT EXISTS (SELECT 1 FROM leave_types WHERE leave_name = 'Sick Leave');

INSERT INTO leave_types (leave_name, days_per_year, is_paid)
SELECT 'Unpaid Leave', 0.00, FALSE WHERE NOT EXISTS (SELECT 1 FROM leave_types WHERE leave_name = 'Unpaid Leave');
