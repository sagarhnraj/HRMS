import React, { useState, useEffect } from 'react';
import axios from 'axios';
import EmployeeDashboard from './EmployeeDashboard';

export default function ManagerDashboard() {
    const [pendingTimesheets, setPendingTimesheets] = useState([]);
    const [pendingLeaves, setPendingLeaves] = useState([]);
    const [teamAttendance, setTeamAttendance] = useState([]);

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        try {
            const [tsRes, attRes, lvRes] = await Promise.all([
                axios.get(import.meta.env.VITE_API_URL + '/api/timesheets/team/pending'),
                axios.get(import.meta.env.VITE_API_URL + '/api/attendance/team'),
                axios.get(import.meta.env.VITE_API_URL + '/api/leave/team/pending')
            ]);
            setPendingTimesheets(tsRes.data);
            setTeamAttendance(attRes.data);
            setPendingLeaves(lvRes.data);
        } catch (e) { console.error('Error fetching manager data', e); }
    };

    const reviewTimesheet = async (id, status) => {
        try {
            await axios.patch(`http://localhost:8080/api/timesheets/${id}/review`, { status });
            fetchData();
        } catch (e) { alert('Error reviewing timesheet: ' + (e.response?.data || e.message)); }
    };

    const reviewLeave = async (id, status) => {
        try {
            await axios.patch(`http://localhost:8080/api/leave/requests/${id}/review`, { status });
            fetchData();
        } catch (e) { alert('Error reviewing leave: ' + (e.response?.data || e.message)); }
    };

    return (
        <div>
            <EmployeeDashboard />
            
            <h2 style={{marginTop: '2.5rem', marginBottom: '1.5rem'}}>Manager Actions</h2>
            <div className="kpi-grid">
                <div className="kpi-card role-manager">
                    <div className="kpi-title">Pending Timesheets</div>
                    <div className="kpi-value">{pendingTimesheets.length}</div>
                </div>
                <div className="kpi-card role-manager">
                    <div className="kpi-title">Pending Leaves</div>
                    <div className="kpi-value">{pendingLeaves.length}</div>
                </div>
                <div className="kpi-card role-manager">
                    <div className="kpi-title">Team Present Today</div>
                    <div className="kpi-value">{teamAttendance.filter(a => a.status === 'PRESENT' || a.status === 'LATE').length}</div>
                </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem', marginTop: '2rem' }}>
                <div style={{ background: 'var(--color-surface)', padding: '1.5rem', border: '1px solid var(--color-border)', borderRadius: '2px' }}>
                    <h3>Pending Timesheets</h3>
                    <div style={{maxHeight: '300px', overflowY: 'auto'}}>
                        <table className="data-table" style={{ marginTop: '1rem' }}>
                            <thead>
                                <tr>
                                    <th>Employee</th>
                                    <th>Date</th>
                                    <th>Hours</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                {pendingTimesheets.length === 0 && <tr><td colSpan="4">No pending timesheets</td></tr>}
                                {pendingTimesheets.map(ts => (
                                    <tr key={ts.timesheetId}>
                                        <td>{ts.employee?.firstName} {ts.employee?.lastName}</td>
                                        <td>{ts.workDate}</td>
                                        <td>{ts.hoursWorked}</td>
                                        <td>
                                            <button className="btn btn-outline" style={{padding: '0.25rem 0.5rem', marginRight: '0.5rem'}} onClick={() => reviewTimesheet(ts.timesheetId, 'APPROVED')}>Approve</button>
                                            <button className="btn btn-outline" style={{padding: '0.25rem 0.5rem'}} onClick={() => reviewTimesheet(ts.timesheetId, 'REJECTED')}>Reject</button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>

                <div style={{ background: 'var(--color-surface)', padding: '1.5rem', border: '1px solid var(--color-border)', borderRadius: '2px' }}>
                    <h3>Pending Leave Requests</h3>
                    <div style={{maxHeight: '300px', overflowY: 'auto'}}>
                        <table className="data-table" style={{ marginTop: '1rem' }}>
                            <thead>
                                <tr>
                                    <th>Employee</th>
                                    <th>Type</th>
                                    <th>Dates</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                {pendingLeaves.length === 0 && <tr><td colSpan="4">No pending leaves</td></tr>}
                                {pendingLeaves.map(lr => (
                                    <tr key={lr.leaveRequestId}>
                                        <td>{lr.employee?.firstName} {lr.employee?.lastName}</td>
                                        <td>{lr.leaveType?.leaveName}</td>
                                        <td>{lr.startDate} to {lr.endDate} ({lr.totalDays}d)</td>
                                        <td>
                                            <button className="btn btn-outline" style={{padding: '0.25rem 0.5rem', marginRight: '0.5rem'}} onClick={() => reviewLeave(lr.leaveRequestId, 'APPROVED')}>Approve</button>
                                            <button className="btn btn-outline" style={{padding: '0.25rem 0.5rem'}} onClick={() => reviewLeave(lr.leaveRequestId, 'REJECTED')}>Reject</button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>

                <div style={{ background: 'var(--color-surface)', padding: '1.5rem', border: '1px solid var(--color-border)', borderRadius: '2px', gridColumn: '1 / -1' }}>
                    <h3>Team Attendance Today</h3>
                    <table className="data-table" style={{ marginTop: '1rem' }}>
                        <thead>
                            <tr>
                                <th>Employee</th>
                                <th>Check In</th>
                                <th>Check Out</th>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            {teamAttendance.length === 0 && <tr><td colSpan="4">No records found for today</td></tr>}
                            {teamAttendance.map(att => (
                                <tr key={att.attendanceId}>
                                    <td>{att.employee?.firstName} {att.employee?.lastName}</td>
                                    <td>{att.checkIn ? new Date(att.checkIn).toLocaleTimeString() : '-'}</td>
                                    <td>{att.checkOut ? new Date(att.checkOut).toLocaleTimeString() : '-'}</td>
                                    <td><span className={`status-badge status-${att.status.toLowerCase()}`}>{att.status}</span></td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
}
