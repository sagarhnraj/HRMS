import React, { useState, useEffect, useContext } from 'react';
import axios from 'axios';
import { AuthContext } from '../context/AuthContext';
import { formatINR } from '../utils/currency';

export default function EmployeeDashboard() {
    const { user } = useContext(AuthContext);
    const [todayAttendance, setTodayAttendance] = useState(null);
    const [timesheets, setTimesheets] = useState([]);
    
    // Log work state
    const [workDate, setWorkDate] = useState(new Date().toISOString().split('T')[0]);
    const [taskDesc, setTaskDesc] = useState('');
    const [hours, setHours] = useState('');

    // Leave state
    const [leaveBalances, setLeaveBalances] = useState([]);
    const [leaveRequests, setLeaveRequests] = useState([]);
    const [leaveTypes, setLeaveTypes] = useState([]);
    const [leaveType, setLeaveType] = useState('');
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');
    const [reason, setReason] = useState('');
    
    // Payslip state
    const [payslips, setPayslips] = useState([]);

    useEffect(() => {
        fetchAttendance();
        fetchTimesheets();
        fetchLeaveData();
        fetchPayslips();
    }, []);

    const fetchAttendance = async () => {
        try {
            const res = await axios.get(import.meta.env.VITE_API_URL + '/api/attendance/me');
            const todayStr = new Date().toISOString().split('T')[0];
            const record = res.data.find(r => r.attendanceDate === todayStr);
            if (record) {
                setTodayAttendance(record);
            }
        } catch (e) { console.error('Failed to fetch attendance', e); }
    };

    const fetchPayslips = async () => {
        try {
            const res = await axios.get(import.meta.env.VITE_API_URL + '/api/payroll/me');
            setPayslips(res.data);
        } catch (e) { console.error('Failed to fetch payslips', e); }
    };

    const fetchTimesheets = async () => {
        try {
            const res = await axios.get(import.meta.env.VITE_API_URL + '/api/timesheets/me');
            setTimesheets(res.data);
        } catch (e) { console.error('Failed to fetch timesheets', e); }
    };

    const fetchLeaveData = async () => {
        try {
            const [bals, reqs, types] = await Promise.all([
                axios.get(import.meta.env.VITE_API_URL + '/api/leave/balances/me'),
                axios.get(import.meta.env.VITE_API_URL + '/api/leave/requests/me'),
                axios.get(import.meta.env.VITE_API_URL + '/api/leave/types')
            ]);
            setLeaveBalances(bals.data);
            setLeaveRequests(reqs.data);
            setLeaveTypes(types.data);
        } catch (e) { console.error('Failed to fetch leave data', e); }
    }

    const handleCheckInOut = async () => {
        try {
            if (!todayAttendance || !todayAttendance.checkIn) {
                await axios.post(import.meta.env.VITE_API_URL + '/api/attendance/check-in');
            } else {
                await axios.post(import.meta.env.VITE_API_URL + '/api/attendance/check-out');
            }
            fetchAttendance();
        } catch (e) {
            alert('Failed: ' + (e.response?.data || e.message));
        }
    };

    const submitTimesheet = async (e) => {
        e.preventDefault();
        try {
            await axios.post(import.meta.env.VITE_API_URL + '/api/timesheets', {
                workDate, taskDescription: taskDesc, hoursWorked: parseFloat(hours)
            });
            setTaskDesc(''); setHours('');
            fetchTimesheets();
        } catch (e) { alert('Failed: ' + (e.response?.data || e.message)); }
    };

    const applyForLeave = async (e) => {
        e.preventDefault();
        try {
            await axios.post(import.meta.env.VITE_API_URL + '/api/leave/requests', {
                leaveType: { leaveTypeId: leaveType },
                startDate, endDate, reason
            });
            setLeaveType(''); setStartDate(''); setEndDate(''); setReason('');
            fetchLeaveData();
        } catch (e) { alert('Failed: ' + (e.response?.data || e.message)); }
    };

    const cancelLeave = async (id) => {
        try {
            await axios.patch(`${import.meta.env.VITE_API_URL}/api/leave/requests/${id}/cancel`);
            fetchLeaveData();
        } catch (e) { alert('Failed: ' + (e.response?.data || e.message)); }
    }

    return (
        <div>
            <div className="kpi-grid">
                <div className="kpi-card role-employee">
                    <div className="kpi-title">Today's Attendance</div>
                    <div className="kpi-value">
                        {!todayAttendance ? 'Not Checked In' : (todayAttendance.checkOut ? 'Checked Out' : 'Checked In')}
                    </div>
                    {todayAttendance && <div style={{marginTop: '0.5rem', fontSize: '0.875rem', color: 'var(--color-text-light)'}}>Status: {todayAttendance.status}</div>}
                </div>
                <div className="kpi-card role-employee">
                    <div className="kpi-title">Leave Balance</div>
                    <div className="kpi-value">
                        {leaveBalances.map(b => (
                            <div key={b.balanceId} style={{fontSize: '0.875rem'}}>
                                {b.leaveType?.leaveName}: {b.remainingDays} days left
                            </div>
                        ))}
                    </div>
                </div>
            </div>

            <div style={{display: 'flex', gap: '1rem', marginBottom: '2rem'}}>
                <button className="btn btn-primary" onClick={handleCheckInOut} disabled={todayAttendance && todayAttendance.checkOut}>
                    {!todayAttendance ? 'Check In' : 'Check Out'}
                </button>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '2rem' }}>
                <div style={{ background: 'var(--color-surface)', padding: '1.5rem', border: '1px solid var(--color-border)', borderRadius: '2px' }}>
                    <h3>Log Work</h3>
                    <form onSubmit={submitTimesheet} style={{ display: 'flex', flexDirection: 'column', gap: '1rem', marginTop: '1rem' }}>
                        <div>
                            <label style={{display:'block', marginBottom:'0.5rem', fontSize:'0.875rem'}}>Date</label>
                            <input type="date" className="form-input" required value={workDate} onChange={e=>setWorkDate(e.target.value)} />
                        </div>
                        <div>
                            <label style={{display:'block', marginBottom:'0.5rem', fontSize:'0.875rem'}}>Task Description</label>
                            <textarea className="form-input" required value={taskDesc} onChange={e=>setTaskDesc(e.target.value)} rows="3" />
                        </div>
                        <div>
                            <label style={{display:'block', marginBottom:'0.5rem', fontSize:'0.875rem'}}>Hours Worked</label>
                            <input type="number" step="0.1" className="form-input" required value={hours} onChange={e=>setHours(e.target.value)} />
                        </div>
                        <button className="btn btn-primary" type="submit">Submit Timesheet</button>
                    </form>
                </div>

                <div style={{ background: 'var(--color-surface)', padding: '1.5rem', border: '1px solid var(--color-border)', borderRadius: '2px' }}>
                    <h3>My Timesheets</h3>
                    <div style={{maxHeight: '300px', overflowY: 'auto'}}>
                        <table className="data-table" style={{ marginTop: '1rem' }}>
                            <thead>
                                <tr>
                                    <th>Date</th>
                                    <th>Task</th>
                                    <th>Hours</th>
                                    <th>Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                {timesheets.map(ts => (
                                    <tr key={ts.timesheetId}>
                                        <td>{ts.workDate}</td>
                                        <td>{ts.taskDescription}</td>
                                        <td>{ts.hoursWorked}</td>
                                        <td><span className={`status-badge status-${ts.status.toLowerCase()}`}>{ts.status}</span></td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>

                <div style={{ background: 'var(--color-surface)', padding: '1.5rem', border: '1px solid var(--color-border)', borderRadius: '2px' }}>
                    <h3>Apply for Leave</h3>
                    <form onSubmit={applyForLeave} style={{ display: 'flex', flexDirection: 'column', gap: '1rem', marginTop: '1rem' }}>
                        <div>
                            <label style={{display:'block', marginBottom:'0.5rem', fontSize:'0.875rem'}}>Leave Type</label>
                            <select className="form-select" required value={leaveType} onChange={e=>setLeaveType(e.target.value)}>
                                <option value="">Select...</option>
                                {leaveTypes.map(t => <option key={t.leaveTypeId} value={t.leaveTypeId}>{t.leaveName}</option>)}
                            </select>
                        </div>
                        <div style={{display:'flex', gap:'0.5rem'}}>
                            <div style={{flex:1}}>
                                <label style={{display:'block', marginBottom:'0.5rem', fontSize:'0.875rem'}}>Start Date</label>
                                <input type="date" className="form-input" required value={startDate} onChange={e=>setStartDate(e.target.value)} />
                            </div>
                            <div style={{flex:1}}>
                                <label style={{display:'block', marginBottom:'0.5rem', fontSize:'0.875rem'}}>End Date</label>
                                <input type="date" className="form-input" required value={endDate} onChange={e=>setEndDate(e.target.value)} />
                            </div>
                        </div>
                        <div>
                            <label style={{display:'block', marginBottom:'0.5rem', fontSize:'0.875rem'}}>Reason</label>
                            <textarea className="form-input" required value={reason} onChange={e=>setReason(e.target.value)} rows="2" />
                        </div>
                        <button className="btn btn-primary" type="submit">Submit Request</button>
                    </form>
                </div>

                <div style={{ background: 'var(--color-surface)', padding: '1.5rem', border: '1px solid var(--color-border)', borderRadius: '2px' }}>
                    <h3>My Leave Requests</h3>
                    <div style={{maxHeight: '300px', overflowY: 'auto'}}>
                        <table className="data-table" style={{ marginTop: '1rem' }}>
                            <thead>
                                <tr>
                                    <th>Type</th>
                                    <th>Dates</th>
                                    <th>Days</th>
                                    <th>Status</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                {leaveRequests.map(lr => (
                                    <tr key={lr.leaveRequestId}>
                                        <td>{lr.leaveType?.leaveName}</td>
                                        <td>{lr.startDate} to {lr.endDate}</td>
                                        <td>{lr.totalDays}</td>
                                        <td><span className={`status-badge status-${lr.status.toLowerCase()}`}>{lr.status}</span></td>
                                        <td>
                                            {lr.status === 'PENDING' && (
                                                <button className="btn btn-outline" style={{padding:'0.25rem 0.5rem'}} onClick={()=>cancelLeave(lr.leaveRequestId)}>Cancel</button>
                                            )}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>

                <div style={{ background: 'var(--color-surface)', padding: '1.5rem', border: '1px solid var(--color-border)', borderRadius: '2px', gridColumn: '1 / -1' }}>
                    <h3>My Payslips</h3>
                    <div style={{maxHeight: '300px', overflowY: 'auto'}}>
                        <table className="data-table" style={{ marginTop: '1rem' }}>
                            <thead>
                                <tr>
                                    <th>Month</th>
                                    <th>Gross Salary</th>
                                    <th>Total Deductions</th>
                                    <th>Net Salary</th>
                                    <th>Status</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                {payslips.length === 0 && <tr><td colSpan="6">No processed payslips found</td></tr>}
                                {payslips.map(ps => (
                                    <tr key={ps.payrollId}>
                                        <td>{ps.payrollMonth}</td>
                                        <td>{formatINR(ps.grossSalary)}</td>
                                        <td>{formatINR(ps.totalDeductions)}</td>
                                        <td><strong>{formatINR(ps.netSalary)}</strong></td>
                                        <td><span className={`status-badge status-${ps.status.toLowerCase()}`}>{ps.status}</span></td>
                                        <td>
                                            <button 
                                                className="btn btn-outline" 
                                                style={{padding:'0.25rem 0.5rem'}}
                                                onClick={async () => {
                                                    try {
                                                        const res = await axios.get(`${import.meta.env.VITE_API_URL}/api/payroll/${ps.payrollId}/payslip`, { responseType: 'blob' });
                                                        const url = window.URL.createObjectURL(new Blob([res.data]));
                                                        const link = document.createElement('a');
                                                        link.href = url;
                                                        link.setAttribute('download', `payslip_${ps.payrollMonth}.pdf`);
                                                        document.body.appendChild(link);
                                                        link.click();
                                                        link.remove();
                                                    } catch (e) {
                                                        alert('Download failed');
                                                    }
                                                }}>
                                                Download PDF
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>

            </div>
        </div>
    );
}
