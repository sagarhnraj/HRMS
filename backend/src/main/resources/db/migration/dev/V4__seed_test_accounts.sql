-- Seed test accounts
-- Ensure employees exist first to satisfy the FK in users table.
INSERT INTO employees (employee_code, first_name, last_name, phone, department_id, designation_id, location_id, shift_id, employment_type, joining_date, status)
VALUES
  ('TST001', 'Test', 'Employee', '000', 1, 1, 1, 1, 'FULL_TIME', CURRENT_DATE, 'ACTIVE'),
  ('TST002', 'Test', 'Manager', '000', 1, 1, 1, 1, 'FULL_TIME', CURRENT_DATE, 'ACTIVE'),
  ('TST003', 'Test', 'HR', '000', 1, 1, 1, 1, 'FULL_TIME', CURRENT_DATE, 'ACTIVE'),
  ('TST004', 'Test', 'Admin', '000', 1, 1, 1, 1, 'FULL_TIME', CURRENT_DATE, 'ACTIVE'),
  ('TST005', 'Test', 'Dual', '000', 1, 1, 1, 1, 'FULL_TIME', CURRENT_DATE, 'ACTIVE')
ON DUPLICATE KEY UPDATE first_name = VALUES(first_name);

-- Hash for Test@1234
INSERT INTO users (email, password_hash, status, employee_id)
SELECT 'employee.test@hrms.local', '$2b$10$8BbPV37N0iPxJTQlFKtRkug0.Ss69.2TmAzDRnC/kumUvkyE5T32q', 'ACTIVE', employee_id FROM employees WHERE employee_code = 'TST001'
ON DUPLICATE KEY UPDATE status = 'ACTIVE';

INSERT INTO users (email, password_hash, status, employee_id)
SELECT 'manager.test@hrms.local', '$2b$10$8BbPV37N0iPxJTQlFKtRkug0.Ss69.2TmAzDRnC/kumUvkyE5T32q', 'ACTIVE', employee_id FROM employees WHERE employee_code = 'TST002'
ON DUPLICATE KEY UPDATE status = 'ACTIVE';

INSERT INTO users (email, password_hash, status, employee_id)
SELECT 'hr.test@hrms.local', '$2b$10$8BbPV37N0iPxJTQlFKtRkug0.Ss69.2TmAzDRnC/kumUvkyE5T32q', 'ACTIVE', employee_id FROM employees WHERE employee_code = 'TST003'
ON DUPLICATE KEY UPDATE status = 'ACTIVE';

INSERT INTO users (email, password_hash, status, employee_id)
SELECT 'admin.test@hrms.local', '$2b$10$8BbPV37N0iPxJTQlFKtRkug0.Ss69.2TmAzDRnC/kumUvkyE5T32q', 'ACTIVE', employee_id FROM employees WHERE employee_code = 'TST004'
ON DUPLICATE KEY UPDATE status = 'ACTIVE';

INSERT INTO users (email, password_hash, status, employee_id)
SELECT 'dual.test@hrms.local', '$2b$10$8BbPV37N0iPxJTQlFKtRkug0.Ss69.2TmAzDRnC/kumUvkyE5T32q', 'ACTIVE', employee_id FROM employees WHERE employee_code = 'TST005'
ON DUPLICATE KEY UPDATE status = 'ACTIVE';

-- Assign Roles
-- Find user IDs
SET @emp_id = (SELECT user_id FROM users WHERE email = 'employee.test@hrms.local');
SET @mgr_id = (SELECT user_id FROM users WHERE email = 'manager.test@hrms.local');
SET @hr_id = (SELECT user_id FROM users WHERE email = 'hr.test@hrms.local');
SET @adm_id = (SELECT user_id FROM users WHERE email = 'admin.test@hrms.local');
SET @dual_id = (SELECT user_id FROM users WHERE email = 'dual.test@hrms.local');

-- Find role IDs
SET @role_emp = (SELECT role_id FROM roles WHERE role_name = 'EMPLOYEE');
SET @role_mgr = (SELECT role_id FROM roles WHERE role_name = 'MANAGER');
SET @role_hr = (SELECT role_id FROM roles WHERE role_name = 'HR');
SET @role_adm = (SELECT role_id FROM roles WHERE role_name = 'ADMIN');

-- Insert User Roles
INSERT IGNORE INTO user_roles (user_id, role_id) VALUES
(@emp_id, @role_emp),
(@mgr_id, @role_mgr),
(@hr_id, @role_hr),
(@adm_id, @role_adm),
(@dual_id, @role_emp),
(@dual_id, @role_mgr);
