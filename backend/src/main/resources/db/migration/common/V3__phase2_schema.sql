-- Stub Tables for Phase 3/4
CREATE TABLE departments (
    department_id INT AUTO_INCREMENT PRIMARY KEY,
    department_name VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE designations (
    designation_id INT AUTO_INCREMENT PRIMARY KEY,
    designation_name VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE office_locations (
    location_id INT AUTO_INCREMENT PRIMARY KEY,
    location_name VARCHAR(100) NOT NULL UNIQUE,
    address VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE shifts (
    shift_id INT AUTO_INCREMENT PRIMARY KEY,
    shift_name VARCHAR(100) NOT NULL UNIQUE,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    grace_minutes INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Seed Default Lookups
INSERT INTO departments (department_name) VALUES ('Human Resources'), ('Sales'), ('Engineering'), ('Finance');
INSERT INTO designations (designation_name) VALUES ('Software Engineer'), ('Sales Executive'), ('HR Executive'), ('Manager');
INSERT INTO office_locations (location_name, address) VALUES ('Head Office', '123 Main St, Tech Park');
INSERT INTO shifts (shift_name, start_time, end_time) VALUES ('General Shift', '09:00:00', '18:00:00');

-- Add new columns as NULLable first to handle existing data
ALTER TABLE employees
ADD COLUMN phone VARCHAR(20),
ADD COLUMN department_id INT,
ADD COLUMN designation_id INT,
ADD COLUMN location_id INT,
ADD COLUMN shift_id INT,
ADD COLUMN manager_id INT,
ADD COLUMN employment_type ENUM('FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERN'),
ADD COLUMN joining_date DATE;

-- Update existing seeded employee(s) with defaults
UPDATE employees SET 
    phone = '000-000-0000',
    department_id = (SELECT department_id FROM departments WHERE department_name = 'Engineering'),
    designation_id = (SELECT designation_id FROM designations WHERE designation_name = 'Manager'),
    location_id = (SELECT location_id FROM office_locations WHERE location_name = 'Head Office'),
    shift_id = (SELECT shift_id FROM shifts WHERE shift_name = 'General Shift'),
    employment_type = 'FULL_TIME',
    joining_date = CURRENT_DATE
WHERE employee_id > 0;

-- Now enforce NOT NULL
ALTER TABLE employees MODIFY phone VARCHAR(20) NOT NULL;
ALTER TABLE employees MODIFY department_id INT NOT NULL;
ALTER TABLE employees MODIFY designation_id INT NOT NULL;
ALTER TABLE employees MODIFY location_id INT NOT NULL;
ALTER TABLE employees MODIFY shift_id INT NOT NULL;
ALTER TABLE employees MODIFY employment_type ENUM('FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERN') NOT NULL;
ALTER TABLE employees MODIFY joining_date DATE NOT NULL;

-- Add Foreign Keys
ALTER TABLE employees ADD CONSTRAINT fk_emp_dept FOREIGN KEY (department_id) REFERENCES departments(department_id);
ALTER TABLE employees ADD CONSTRAINT fk_emp_desig FOREIGN KEY (designation_id) REFERENCES designations(designation_id);
ALTER TABLE employees ADD CONSTRAINT fk_emp_loc FOREIGN KEY (location_id) REFERENCES office_locations(location_id);
ALTER TABLE employees ADD CONSTRAINT fk_emp_shift FOREIGN KEY (shift_id) REFERENCES shifts(shift_id);
ALTER TABLE employees ADD CONSTRAINT fk_emp_mgr FOREIGN KEY (manager_id) REFERENCES employees(employee_id);
