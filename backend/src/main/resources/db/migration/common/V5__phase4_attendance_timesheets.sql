-- Phase 4 schema for Attendance & Timesheets
CREATE TABLE IF NOT EXISTS attendance_records (
    attendance_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    attendance_date DATE NOT NULL,
    check_in TIMESTAMP NULL,
    check_out TIMESTAMP NULL,
    status ENUM('PRESENT', 'ABSENT', 'LATE', 'HALF_DAY', 'ON_LEAVE') NOT NULL,
    overtime_minutes INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_att_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id),
    UNIQUE KEY uk_emp_date (employee_id, attendance_date)
);

CREATE TABLE IF NOT EXISTS timesheets (
    timesheet_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    work_date DATE NOT NULL,
    task_description VARCHAR(500) NOT NULL,
    hours_worked DECIMAL(5,2) NOT NULL,
    status ENUM('SUBMITTED', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'SUBMITTED',
    reviewed_by INT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_ts_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id),
    CONSTRAINT fk_ts_reviewer FOREIGN KEY (reviewed_by) REFERENCES employees(employee_id)
);

-- Permissions
INSERT IGNORE INTO permissions (permission_code) VALUES
('SHIFT_MANAGE'),
('ATTENDANCE_MARK'),
('TIMESHEET_SUBMIT'),
('TIMESHEET_REVIEW_TEAM');

-- Retrieve Role IDs
SET @role_emp = (SELECT role_id FROM roles WHERE role_name = 'EMPLOYEE');
SET @role_mgr = (SELECT role_id FROM roles WHERE role_name = 'MANAGER');
SET @role_hr = (SELECT role_id FROM roles WHERE role_name = 'HR');
SET @role_adm = (SELECT role_id FROM roles WHERE role_name = 'ADMIN');

-- Retrieve Permission IDs
SET @perm_shift = (SELECT permission_id FROM permissions WHERE permission_code = 'SHIFT_MANAGE');
SET @perm_att = (SELECT permission_id FROM permissions WHERE permission_code = 'ATTENDANCE_MARK');
SET @perm_ts_sub = (SELECT permission_id FROM permissions WHERE permission_code = 'TIMESHEET_SUBMIT');
SET @perm_ts_rev = (SELECT permission_id FROM permissions WHERE permission_code = 'TIMESHEET_REVIEW_TEAM');

-- Assign Permissions
INSERT IGNORE INTO role_permissions (role_id, permission_id) VALUES
-- Admin and HR can manage shifts
(@role_adm, @perm_shift),
(@role_hr, @perm_shift),
-- Everyone can mark attendance and submit timesheets
(@role_emp, @perm_att),
(@role_mgr, @perm_att),
(@role_hr, @perm_att),
(@role_adm, @perm_att),
(@role_emp, @perm_ts_sub),
(@role_mgr, @perm_ts_sub),
(@role_hr, @perm_ts_sub),
(@role_adm, @perm_ts_sub),
-- Managers can review team timesheets
(@role_mgr, @perm_ts_rev);
