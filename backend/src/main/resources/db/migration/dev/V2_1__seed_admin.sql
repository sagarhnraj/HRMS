
-- Seed Stub Employee
INSERT INTO employees (employee_code, first_name, last_name, status) 
VALUES ('EMP001', 'Admin', 'User', 'ACTIVE');

-- Seed Admin User (password is 'admin123')
INSERT INTO users (employee_id, email, password_hash, status) 
VALUES ((SELECT employee_id FROM employees WHERE employee_code = 'EMP001'), 'admin@hrms.local', '$2a$10$C8.M/6Q1E1nI8/VXY5Q2n.E3lM5rR4L0B9kO/w3x2Kj/2.9sO0K.e', 'ACTIVE');

-- Assign ADMIN role to the user
INSERT INTO user_roles (user_id, role_id)
SELECT u.user_id, r.role_id FROM users u, roles r 
WHERE u.email = 'admin@hrms.local' AND r.role_name = 'ADMIN';

-- Also assign EMPLOYEE and MANAGER and HR roles to the admin user for testing purposes
INSERT INTO user_roles (user_id, role_id)
SELECT u.user_id, r.role_id FROM users u, roles r 
WHERE u.email = 'admin@hrms.local' AND r.role_name IN ('EMPLOYEE', 'MANAGER', 'HR');
