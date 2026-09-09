import React, { useState, useEffect, useContext } from 'react';
import axios from 'axios';
import { AuthContext } from '../context/AuthContext';
import { formatINR } from '../utils/currency';

export default function HrDashboard() {
    const { user } = useContext(AuthContext);
    const [employees, setEmployees] = useState([]);
    const [search, setSearch] = useState('');

    const [departments, setDepartments] = useState([]);
    const [designations, setDesignations] = useState([]);
    const [locations, setLocations] = useState([]);
    const [shifts, setShifts] = useState([]);

    const [showForm, setShowForm] = useState(false);
    const [formData, setFormData] = useState({
        firstName: '', lastName: '', employeeCode: '', phone: '',
        department: { departmentId: '' }, designation: { designationId: '' },
        location: { locationId: '' }, shift: { shiftId: '' },
        employmentType: 'FULL_TIME', joiningDate: '', status: 'ACTIVE'
    });

    // Payroll state
    const [payrollMonth, setPayrollMonth] = useState(new Date().toISOString().substring(0, 7));
    const [payrolls, setPayrolls] = useState([]);
    const [selectedEmployeeSalary, setSelectedEmployeeSalary] = useState(null);
    const [salaryForm, setSalaryForm] = useState({ basicSalary: 0, houseAllowance: 0, travelAllowance: 0, pfDeduction: 0, insuranceDeduction: 0 });

    useEffect(() => {
        if (user.permissions.includes('EMPLOYEE_CREATE') || user.permissions.includes('EMPLOYEE_UPDATE')) {
            fetchLookups();
            fetchEmployees();
        }
        if (user.permissions.includes('SALARY_MANAGE')) {
            fetchPayrolls(payrollMonth);
        }
    }, [user, payrollMonth]);

    const fetchLookups = async () => {
        try {
            const [dept, desig, loc, sh] = await Promise.all([
                axios.get(import.meta.env.VITE_API_URL + '/api/lookups/departments'),
                axios.get(import.meta.env.VITE_API_URL + '/api/lookups/designations'),
                axios.get(import.meta.env.VITE_API_URL + '/api/lookups/locations'),
                axios.get(import.meta.env.VITE_API_URL + '/api/lookups/shifts')
            ]);
            setDepartments(dept.data); setDesignations(desig.data);
            setLocations(loc.data); setShifts(sh.data);
        } catch (e) { console.error('Error fetching lookups', e); }
    };

    const fetchEmployees = async () => {
        try {
            const res = await axios.get(import.meta.env.VITE_API_URL + '/api/employees');
            setEmployees(res.data);
        } catch (e) { console.error('Error fetching employees', e); }
    };

    const fetchPayrolls = async (month) => {
        try {
            const res = await axios.get(`${import.meta.env.VITE_API_URL}/api/payroll/month?month=${month}`);
            setPayrolls(res.data);
        } catch (e) { console.error('Error fetching payrolls', e); }
    };

    const handleCreate = async (e) => {
        e.preventDefault();
        try {
            await axios.post(import.meta.env.VITE_API_URL + '/api/employees', formData);
            setShowForm(false);
            fetchEmployees();
        } catch (e) { console.error('Error creating employee', e); }
    };

    const loadSalary = async (empId) => {
        try {
            const res = await axios.get(`${import.meta.env.VITE_API_URL}/api/payroll/salary/${empId}`);
            setSalaryForm(res.data);
            setSelectedEmployeeSalary(empId);
        } catch (e) {
            setSalaryForm({ basicSalary: 0, houseAllowance: 0, travelAllowance: 0, pfDeduction: 0, insuranceDeduction: 0 });
            setSelectedEmployeeSalary(empId);
        }
    };

    const saveSalary = async (e) => {
        e.preventDefault();
        try {
            await axios.post(`${import.meta.env.VITE_API_URL}/api/payroll/salary/${selectedEmployeeSalary}`, salaryForm);
            alert("Salary structure updated");
            setSelectedEmployeeSalary(null);
        } catch (e) { alert("Failed: " + e.message); }
    };

    const generatePayroll = async () => {
        try {
            await axios.post(`${import.meta.env.VITE_API_URL}/api/payroll/generate?month=${payrollMonth}`);
            fetchPayrolls(payrollMonth);
        } catch (e) { alert("Failed: " + e.message); }
    };

    const updatePayrollItem = async (id, field, value) => {
        try {
            await axios.patch(`${import.meta.env.VITE_API_URL}/api/payroll/${id}`, { [field]: parseFloat(value) || 0 });
            fetchPayrolls(payrollMonth);
        } catch (e) { alert("Failed: " + e.message); }
    };

    const processPayroll = async (id) => {
        try {
            await axios.patch(`${import.meta.env.VITE_API_URL}/api/payroll/${id}/process`);
            fetchPayrolls(payrollMonth);
        } catch (e) { alert("Failed: " + e.message); }
    };

    const payPayroll = async (id) => {
        try {
            await axios.patch(`${import.meta.env.VITE_API_URL}/api/payroll/${id}/pay`);
            fetchPayrolls(payrollMonth);
        } catch (e) { alert("Failed: " + e.message); }
    };

    const filteredEmployees = employees.filter(emp => 
        (emp.firstName + ' ' + emp.lastName).toLowerCase().includes(search.toLowerCase()) ||
        emp.employeeCode.toLowerCase().includes(search.toLowerCase())
    );

    return (
        <div>
            <div className="kpi-grid">
                <div className="kpi-card role-hr">
                    <div className="kpi-title">Org Structure</div>
                    <div className="kpi-value">View Diagram</div>
                </div>
                <div className="kpi-card role-hr">
                    <div className="kpi-title">Leave Policies</div>
                    <div className="kpi-value">Manage</div>
                </div>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', margin: '2.5rem 0 1.5rem 0' }}>
                <h2>Employee Directory</h2>
                {user.permissions.includes('EMPLOYEE_CREATE') && (
                    <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}>
                        {showForm ? 'Cancel' : 'Add Employee'}
                    </button>
                )}
            </div>

            {showForm && (
                <div style={{ background: 'var(--color-surface)', padding: '1.5rem', marginBottom: '2rem', border: '1px solid var(--color-border)', borderRadius: '2px' }}>
                    <form onSubmit={handleCreate} style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                        <div className="form-group"><label className="form-label">Employee Code</label><input required className="form-input" value={formData.employeeCode} onChange={e=>setFormData({...formData, employeeCode: e.target.value})} /></div>
                        <div className="form-group"><label className="form-label">First Name</label><input required className="form-input" value={formData.firstName} onChange={e=>setFormData({...formData, firstName: e.target.value})} /></div>
                        <div className="form-group"><label className="form-label">Last Name</label><input required className="form-input" value={formData.lastName} onChange={e=>setFormData({...formData, lastName: e.target.value})} /></div>
                        <div className="form-group"><label className="form-label">Phone</label><input required className="form-input" value={formData.phone} onChange={e=>setFormData({...formData, phone: e.target.value})} /></div>
                        <div className="form-group"><label className="form-label">Department</label><select required className="form-select" value={formData.department.departmentId} onChange={e=>setFormData({...formData, department: {departmentId: e.target.value}})}><option value="">Select</option>{departments.map(d=><option key={d.departmentId} value={d.departmentId}>{d.departmentName}</option>)}</select></div>
                        <div className="form-group"><label className="form-label">Designation</label><select required className="form-select" value={formData.designation.designationId} onChange={e=>setFormData({...formData, designation: {designationId: e.target.value}})}><option value="">Select</option>{designations.map(d=><option key={d.designationId} value={d.designationId}>{d.designationName}</option>)}</select></div>
                        <div className="form-group"><label className="form-label">Location</label><select required className="form-select" value={formData.location.locationId} onChange={e=>setFormData({...formData, location: {locationId: e.target.value}})}><option value="">Select</option>{locations.map(d=><option key={d.locationId} value={d.locationId}>{d.locationName}</option>)}</select></div>
                        <div className="form-group"><label className="form-label">Shift</label><select required className="form-select" value={formData.shift.shiftId} onChange={e=>setFormData({...formData, shift: {shiftId: e.target.value}})}><option value="">Select</option>{shifts.map(d=><option key={d.shiftId} value={d.shiftId}>{d.shiftName}</option>)}</select></div>
                        <div className="form-group"><label className="form-label">Joining Date</label><input required type="date" className="form-input" value={formData.joiningDate} onChange={e=>setFormData({...formData, joiningDate: e.target.value})} /></div>
                        <div className="form-group" style={{gridColumn: '1 / -1'}}><button type="submit" className="btn btn-primary">Save Employee</button></div>
                    </form>
                </div>
            )}

            <div className="form-group">
                <input type="text" className="form-input" placeholder="Search by name or code..." value={search} onChange={e => setSearch(e.target.value)} style={{maxWidth: '400px'}} />
            </div>

            <div className="table-container">
                <table className="table">
                    <thead><tr><th>Code</th><th>Name</th><th>Department</th><th>Designation</th><th>Status</th><th>Actions</th></tr></thead>
                    <tbody>
                        {filteredEmployees.map(emp => (
                            <tr key={emp.employeeId}>
                                <td>{emp.employeeCode}</td>
                                <td>{emp.firstName} {emp.lastName}</td>
                                <td>{emp.department?.departmentName}</td>
                                <td>{emp.designation?.designationName}</td>
                                <td><span className={`status-badge status-${emp.status.toLowerCase()}`}>{emp.status}</span></td>
                                <td>
                                    {user.permissions.includes('EMPLOYEE_UPDATE') && <button className="btn btn-outline" style={{padding: '0.25rem 0.5rem', marginRight: '0.5rem'}}>Edit</button>}
                                    {user.permissions.includes('SALARY_MANAGE') && <button className="btn btn-outline" style={{padding: '0.25rem 0.5rem'}} onClick={() => loadSalary(emp.employeeId)}>Salary Struct</button>}
                                </td>
                            </tr>
                        ))}
                        {filteredEmployees.length === 0 && <tr><td colSpan="6" style={{textAlign:'center'}}>No employees found</td></tr>}
                    </tbody>
                </table>
            </div>

            {selectedEmployeeSalary && (
                <div style={{ background: 'var(--color-surface)', padding: '1.5rem', marginTop: '2rem', border: '1px solid var(--color-border)', borderRadius: '2px' }}>
                    <h3>Edit Salary Structure</h3>
                    <form onSubmit={saveSalary} style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginTop: '1rem' }}>
                        <div className="form-group"><label className="form-label">Basic Salary (INR)</label><input type="number" step="0.01" required className="form-input" value={salaryForm.basicSalary} onChange={e=>setSalaryForm({...salaryForm, basicSalary: e.target.value})} /></div>
                        <div className="form-group"><label className="form-label">House Allowance (INR)</label><input type="number" step="0.01" required className="form-input" value={salaryForm.houseAllowance} onChange={e=>setSalaryForm({...salaryForm, houseAllowance: e.target.value})} /></div>
                        <div className="form-group"><label className="form-label">Travel Allowance (INR)</label><input type="number" step="0.01" required className="form-input" value={salaryForm.travelAllowance} onChange={e=>setSalaryForm({...salaryForm, travelAllowance: e.target.value})} /></div>
                        <div className="form-group"><label className="form-label">PF Deduction (INR)</label><input type="number" step="0.01" required className="form-input" value={salaryForm.pfDeduction} onChange={e=>setSalaryForm({...salaryForm, pfDeduction: e.target.value})} /></div>
                        <div className="form-group"><label className="form-label">Insurance Deduction (INR)</label><input type="number" step="0.01" required className="form-input" value={salaryForm.insuranceDeduction} onChange={e=>setSalaryForm({...salaryForm, insuranceDeduction: e.target.value})} /></div>
                        <div className="form-group" style={{gridColumn: '1 / -1'}}>
                            <button type="submit" className="btn btn-primary" style={{marginRight: '1rem'}}>Save Structure</button>
                            <button type="button" className="btn btn-outline" onClick={() => setSelectedEmployeeSalary(null)}>Cancel</button>
                        </div>
                    </form>
                </div>
            )}

            {user.permissions.includes('SALARY_MANAGE') && (
                <div style={{ marginTop: '3rem' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
                        <h2>Payroll Processing</h2>
                        <div style={{ display: 'flex', gap: '1rem' }}>
                            <input type="month" className="form-input" value={payrollMonth} onChange={e => setPayrollMonth(e.target.value)} />
                            <button className="btn btn-primary" onClick={generatePayroll}>Generate Payroll</button>
                        </div>
                    </div>
                    
                    <div className="table-container" style={{overflowX: 'auto'}}>
                        <table className="table" style={{minWidth: '1000px'}}>
                            <thead>
                                <tr>
                                    <th>Employee</th>
                                    <th>Gross</th>
                                    <th>Inc. (Edit)</th>
                                    <th>Tax (Edit)</th>
                                    <th>Loan (Edit)</th>
                                    <th>Deduct</th>
                                    <th>Net</th>
                                    <th>Status</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {payrolls.length === 0 && <tr><td colSpan="9" style={{textAlign:'center'}}>No payroll generated for this month</td></tr>}
                                {payrolls.map(pr => (
                                    <tr key={pr.payrollId}>
                                        <td>{pr.employee?.firstName} {pr.employee?.lastName}</td>
                                        <td>{formatINR(pr.grossSalary)}</td>
                                        <td>
                                            <input type="number" className="form-input" style={{width:'80px', padding:'0.25rem'}} value={pr.incentiveAmount} disabled={pr.status !== 'DRAFT'} 
                                                onBlur={(e) => updatePayrollItem(pr.payrollId, 'incentiveAmount', e.target.value)}
                                                onChange={(e) => {
                                                    const newPayrolls = [...payrolls];
                                                    const idx = newPayrolls.findIndex(p => p.payrollId === pr.payrollId);
                                                    newPayrolls[idx].incentiveAmount = e.target.value;
                                                    setPayrolls(newPayrolls);
                                                }} />
                                        </td>
                                        <td>
                                            <input type="number" className="form-input" style={{width:'80px', padding:'0.25rem'}} value={pr.taxAmount} disabled={pr.status !== 'DRAFT'} 
                                                onBlur={(e) => updatePayrollItem(pr.payrollId, 'taxAmount', e.target.value)}
                                                onChange={(e) => {
                                                    const newPayrolls = [...payrolls];
                                                    const idx = newPayrolls.findIndex(p => p.payrollId === pr.payrollId);
                                                    newPayrolls[idx].taxAmount = e.target.value;
                                                    setPayrolls(newPayrolls);
                                                }} />
                                        </td>
                                        <td>
                                            <input type="number" className="form-input" style={{width:'80px', padding:'0.25rem'}} value={pr.loanAmount} disabled={pr.status !== 'DRAFT'} 
                                                onBlur={(e) => updatePayrollItem(pr.payrollId, 'loanAmount', e.target.value)}
                                                onChange={(e) => {
                                                    const newPayrolls = [...payrolls];
                                                    const idx = newPayrolls.findIndex(p => p.payrollId === pr.payrollId);
                                                    newPayrolls[idx].loanAmount = e.target.value;
                                                    setPayrolls(newPayrolls);
                                                }} />
                                        </td>
                                        <td>{formatINR(pr.totalDeductions)}</td>
                                        <td><strong>{formatINR(pr.netSalary)}</strong></td>
                                        <td><span className={`status-badge status-${pr.status.toLowerCase()}`}>{pr.status}</span></td>
                                        <td>
                                            {pr.status === 'DRAFT' && <button className="btn btn-outline" style={{padding: '0.25rem 0.5rem'}} onClick={() => processPayroll(pr.payrollId)}>Process</button>}
                                            {pr.status === 'PROCESSED' && <button className="btn btn-outline" style={{padding: '0.25rem 0.5rem'}} onClick={() => payPayroll(pr.payrollId)}>Pay</button>}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}
        </div>
    );
}
