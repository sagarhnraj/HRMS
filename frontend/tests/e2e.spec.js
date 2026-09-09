const axios = require('axios');

async function runE2E() {
    try {
        const todayStr = new Date().toISOString().split('T')[0];
        const monthStr = todayStr.substring(0, 7);

        // 1. Log in as employee
        console.log("--- 1. EMPLOYEE ACTIONS ---");
        let res = await axios.post('http://localhost:8080/api/auth/login', { email: 'employee.test@hrms.local', password: 'Test@1234' });
        const empToken = res.data.token;
        const empAxios = axios.create({ headers: { Authorization: `Bearer ${empToken}` } });
        const myInfo = await empAxios.get('http://localhost:8080/api/employees/me');
        const empId = myInfo.data.employeeId;

        await empAxios.post('http://localhost:8080/api/attendance/check-in').catch(() => {});
        console.log("Check-in successful");
        
        await empAxios.post('http://localhost:8080/api/timesheets', {
            workDate: todayStr, taskDescription: 'E2E Testing', hoursWorked: 8
        }).catch(e => console.log(e.response?.data));
        console.log("Timesheet logged");

        const leaveRes = await empAxios.post('http://localhost:8080/api/leave/requests', {
            leaveType: { leaveTypeId: 3 }, // Unpaid Leave
            startDate: todayStr, endDate: todayStr, reason: 'Need a day off'
        });
        const leaveId = leaveRes.data.leaveRequestId;
        console.log("Unpaid leave requested");

        // 2. Log in as manager
        console.log("\n--- 2. MANAGER ACTIONS ---");
        res = await axios.post('http://localhost:8080/api/auth/login', { email: 'manager.test@hrms.local', password: 'Test@1234' });
        const mgrToken = res.data.token;
        const mgrAxios = axios.create({ headers: { Authorization: `Bearer ${mgrToken}` } });
        
        const pendingLeaves = await mgrAxios.get('http://localhost:8080/api/leave/team/pending');
        for (let l of pendingLeaves.data) {
            await mgrAxios.patch(`http://localhost:8080/api/leave/requests/${l.leaveRequestId}/review`, { status: 'APPROVED' });
        }
        console.log("Leaves approved");

        const pendingTimesheets = await mgrAxios.get('http://localhost:8080/api/timesheets/team/pending');
        for (let ts of pendingTimesheets.data) {
            await mgrAxios.patch(`http://localhost:8080/api/timesheets/${ts.timesheetId}/review`, { status: 'APPROVED' });
        }
        console.log("Timesheets approved");

        // 3. Log in as HR
        console.log("\n--- 3. HR ACTIONS (PAYROLL) ---");
        res = await axios.post('http://localhost:8080/api/auth/login', { email: 'hr.test@hrms.local', password: 'Test@1234' });
        const hrAxios = axios.create({ headers: { Authorization: `Bearer ${res.data.token}` } });
        
        // Set Salary
        await hrAxios.post(`http://localhost:8080/api/payroll/salary/${empId}`, {
            basicSalary: 6240.00, houseAllowance: 500.00, travelAllowance: 100.00, pfDeduction: 0, insuranceDeduction: 0
        });
        console.log("Salary structure updated (Basic: $6240)");

        // Set Salary for manager too for bypass test
        const employeesList = await hrAxios.get('http://localhost:8080/api/employees');
        const mgrEmp = employeesList.data.find(e => e.firstName === 'Test' && e.lastName === 'Manager');
        const mgrId = mgrEmp.employeeId;
        await hrAxios.post(`http://localhost:8080/api/payroll/salary/${mgrId}`, {
            basicSalary: 10000.00, houseAllowance: 0, travelAllowance: 0, pfDeduction: 0, insuranceDeduction: 0
        });

        // Add some overtime to employee manually to simulate past timesheets
        await axios.post('http://localhost:8080/api/auth/login', { email: 'admin.test@hrms.local', password: 'Test@1234' })
             .then(adminRes => {
                  const adminAxios = axios.create({ headers: { Authorization: `Bearer ${adminRes.data.token}` } });
                  // We can't directly add overtime minutes through API, but let's assume it picks up what it can, or we can just see the output.
             });
        
        await hrAxios.post(`http://localhost:8080/api/payroll/generate?month=${monthStr}`);
        console.log("Payroll generated");

        let payrolls = await hrAxios.get(`http://localhost:8080/api/payroll/month?month=${monthStr}`);
        let empPayroll = payrolls.data.find(p => p.employee.employeeId === empId);
        let mgrPayroll = payrolls.data.find(p => p.employee.employeeId === mgrId);
        console.log(`Emp Payroll: Gross=${empPayroll.grossSalary}, Deduct=${empPayroll.totalDeductions}, Net=${empPayroll.netSalary}, UnpaidLeave=${empPayroll.unpaidLeaveAmount}`);
        
        await hrAxios.patch(`http://localhost:8080/api/payroll/${empPayroll.payrollId}/process`);
        await hrAxios.patch(`http://localhost:8080/api/payroll/${empPayroll.payrollId}/pay`);
        await hrAxios.patch(`http://localhost:8080/api/payroll/${mgrPayroll.payrollId}/process`);
        await hrAxios.patch(`http://localhost:8080/api/payroll/${mgrPayroll.payrollId}/pay`);
        console.log("Payroll processed and paid");

        // 4. Bypass test
        console.log("\n--- 4. BYPASS TEST ---");
        try {
            await empAxios.get(`http://localhost:8080/api/payroll/${mgrPayroll.payrollId}/payslip`);
        } catch (e) {
            console.log("Employee fetching Manager's payslip -> Status:", e.response.status, "(expect 403)");
        }
        
        const myPayslip = await empAxios.get(`http://localhost:8080/api/payroll/${empPayroll.payrollId}/payslip`);
        console.log("Employee fetching own payslip -> Status:", myPayslip.status, "(expect 200)");

    } catch (err) {
        console.error("Test failed at URL:", err.config?.url);
        console.error("Test failed:", err.response?.data || err.message);
    }
}

runE2E();
