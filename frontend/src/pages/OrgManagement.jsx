import React, { useState, useEffect, useContext } from 'react';
import axios from 'axios';
import { AuthContext } from '../context/AuthContext';

export default function OrgManagement() {
    const { user } = useContext(AuthContext);
    const [departments, setDepartments] = useState([]);
    const [designations, setDesignations] = useState([]);
    const [locations, setLocations] = useState([]);
    const [shifts, setShifts] = useState([]);
    const [leaveTypes, setLeaveTypes] = useState([]);

    const [newDept, setNewDept] = useState('');
    const [newDesig, setNewDesig] = useState('');
    const [newLocName, setNewLocName] = useState('');
    const [newLocAddress, setNewLocAddress] = useState('');
    const [newShift, setNewShift] = useState({ shiftName: '', startTime: '', endTime: '', graceMinutes: 0 });
    const [newLeave, setNewLeave] = useState({ leaveName: '', daysPerYear: '', isPaid: true });

    useEffect(() => {
        fetchLookups();
    }, []);

    const fetchLookups = async () => {
        try {
            const [dept, desig, loc, shf, lvt] = await Promise.all([
                axios.get(import.meta.env.VITE_API_URL + '/api/lookups/departments'),
                axios.get(import.meta.env.VITE_API_URL + '/api/lookups/designations'),
                axios.get(import.meta.env.VITE_API_URL + '/api/lookups/locations'),
                axios.get(import.meta.env.VITE_API_URL + '/api/lookups/shifts'),
                axios.get(import.meta.env.VITE_API_URL + '/api/leave/types')
            ]);
            setDepartments(dept.data); setDesignations(desig.data); setLocations(loc.data); 
            setShifts(shf.data); setLeaveTypes(lvt.data);
        } catch (e) { console.error('Error fetching lookups', e); }
    };

    const addDepartment = async (e) => {
        e.preventDefault();
        try {
            await axios.post(import.meta.env.VITE_API_URL + '/api/org/departments', { departmentName: newDept });
            setNewDept(''); fetchLookups();
        } catch (e) { console.error(e); }
    };

    const addDesignation = async (e) => {
        e.preventDefault();
        try {
            await axios.post(import.meta.env.VITE_API_URL + '/api/org/designations', { designationName: newDesig });
            setNewDesig(''); fetchLookups();
        } catch (e) { console.error(e); }
    };

    const addLocation = async (e) => {
        e.preventDefault();
        try {
            await axios.post(import.meta.env.VITE_API_URL + '/api/org/locations', { locationName: newLocName, address: newLocAddress });
            setNewLocName(''); setNewLocAddress(''); fetchLookups();
        } catch (e) { console.error(e); }
    };

    const addShift = async (e) => {
        e.preventDefault();
        try {
            const payload = {
                shiftName: newShift.shiftName,
                startTime: newShift.startTime + ":00", 
                endTime: newShift.endTime + ":00",
                graceMinutes: parseInt(newShift.graceMinutes, 10)
            };
            await axios.post(import.meta.env.VITE_API_URL + '/api/org/shifts', payload);
            setNewShift({ shiftName: '', startTime: '', endTime: '', graceMinutes: 0 }); 
            fetchLookups();
        } catch (e) { console.error(e); }
    };

    const addLeaveType = async (e) => {
        e.preventDefault();
        try {
            await axios.post(import.meta.env.VITE_API_URL + '/api/leave/types', {
                leaveName: newLeave.leaveName,
                daysPerYear: parseFloat(newLeave.daysPerYear),
                isPaid: newLeave.isPaid
            });
            setNewLeave({ leaveName: '', daysPerYear: '', isPaid: true });
            fetchLookups();
        } catch (e) { console.error(e); }
    };

    const initBalances = async () => {
        try {
            const currentYear = new Date().getFullYear();
            const res = await axios.post(`http://localhost:8080/api/leave/balances/initialize?year=${currentYear}`);
            alert(res.data);
        } catch (e) { alert('Failed: ' + (e.response?.data || e.message)); }
    };

    if (!user.permissions.includes('ROLE_MANAGE') && !user.permissions.includes('EMPLOYEE_CREATE')) {
        return <div>Access Denied</div>;
    }

    return (
        <div>
            <h2>Organization Management</h2>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem', marginTop: '2rem' }}>
                
                {/* Departments */}
                <div style={{ background: 'var(--color-surface)', padding: '1.5rem', border: '1px solid var(--color-border)', borderRadius: '2px' }}>
                    <h3>Departments</h3>
                    <ul style={{ margin: '1rem 0', paddingLeft: '1.5rem', maxHeight: '150px', overflowY: 'auto' }}>
                        {departments.map(d => <li key={d.departmentId}>{d.departmentName}</li>)}
                    </ul>
                    <form onSubmit={addDepartment} style={{ display: 'flex', gap: '0.5rem' }}>
                        <input required className="form-input" placeholder="New Department" value={newDept} onChange={e=>setNewDept(e.target.value)} />
                        <button className="btn btn-primary" type="submit">Add</button>
                    </form>
                </div>

                {/* Designations */}
                <div style={{ background: 'var(--color-surface)', padding: '1.5rem', border: '1px solid var(--color-border)', borderRadius: '2px' }}>
                    <h3>Designations</h3>
                    <ul style={{ margin: '1rem 0', paddingLeft: '1.5rem', maxHeight: '150px', overflowY: 'auto' }}>
                        {designations.map(d => <li key={d.designationId}>{d.designationName}</li>)}
                    </ul>
                    <form onSubmit={addDesignation} style={{ display: 'flex', gap: '0.5rem' }}>
                        <input required className="form-input" placeholder="New Designation" value={newDesig} onChange={e=>setNewDesig(e.target.value)} />
                        <button className="btn btn-primary" type="submit">Add</button>
                    </form>
                </div>

                {/* Locations */}
                <div style={{ background: 'var(--color-surface)', padding: '1.5rem', border: '1px solid var(--color-border)', borderRadius: '2px' }}>
                    <h3>Office Locations</h3>
                    <ul style={{ margin: '1rem 0', paddingLeft: '1.5rem', maxHeight: '150px', overflowY: 'auto' }}>
                        {locations.map(l => <li key={l.locationId}>{l.locationName} - <small>{l.address}</small></li>)}
                    </ul>
                    <form onSubmit={addLocation} style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                        <input required className="form-input" placeholder="Location Name" value={newLocName} onChange={e=>setNewLocName(e.target.value)} />
                        <input required className="form-input" placeholder="Address" value={newLocAddress} onChange={e=>setNewLocAddress(e.target.value)} />
                        <button className="btn btn-primary" type="submit">Add Location</button>
                    </form>
                </div>

                {/* Shifts */}
                <div style={{ background: 'var(--color-surface)', padding: '1.5rem', border: '1px solid var(--color-border)', borderRadius: '2px' }}>
                    <h3>Shifts</h3>
                    <ul style={{ margin: '1rem 0', paddingLeft: '1.5rem', maxHeight: '150px', overflowY: 'auto' }}>
                        {shifts.map(s => <li key={s.shiftId}>{s.shiftName} ({s.startTime} - {s.endTime}) grace: {s.graceMinutes}m</li>)}
                    </ul>
                    {user.permissions.includes('SHIFT_MANAGE') && (
                        <form onSubmit={addShift} style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                            <input required className="form-input" placeholder="Shift Name (e.g. Morning)" value={newShift.shiftName} onChange={e=>setNewShift({...newShift, shiftName: e.target.value})} />
                            <div style={{ display: 'flex', gap: '0.5rem' }}>
                                <input required type="time" className="form-input" value={newShift.startTime} onChange={e=>setNewShift({...newShift, startTime: e.target.value})} />
                                <input required type="time" className="form-input" value={newShift.endTime} onChange={e=>setNewShift({...newShift, endTime: e.target.value})} />
                            </div>
                            <input required type="number" min="0" className="form-input" placeholder="Grace Minutes" value={newShift.graceMinutes} onChange={e=>setNewShift({...newShift, graceMinutes: e.target.value})} />
                            <button className="btn btn-primary" type="submit">Add Shift</button>
                        </form>
                    )}
                </div>

                {/* Leave Policies */}
                <div style={{ background: 'var(--color-surface)', padding: '1.5rem', border: '1px solid var(--color-border)', borderRadius: '2px', gridColumn: '1 / -1' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <h3>Leave Policies</h3>
                        {user.permissions.includes('LEAVE_POLICY_MANAGE') && (
                            <button className="btn btn-outline" onClick={initBalances}>Initialize Balances ({new Date().getFullYear()})</button>
                        )}
                    </div>
                    <ul style={{ margin: '1rem 0', paddingLeft: '1.5rem', maxHeight: '150px', overflowY: 'auto' }}>
                        {leaveTypes.map(l => <li key={l.leaveTypeId}>{l.leaveName} - {l.daysPerYear} days ({l.isPaid ? 'Paid' : 'Unpaid'})</li>)}
                    </ul>
                    {user.permissions.includes('LEAVE_POLICY_MANAGE') && (
                        <form onSubmit={addLeaveType} style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
                            <input required className="form-input" placeholder="Leave Name" value={newLeave.leaveName} onChange={e=>setNewLeave({...newLeave, leaveName: e.target.value})} />
                            <input required type="number" step="0.5" className="form-input" placeholder="Days/Year" value={newLeave.daysPerYear} onChange={e=>setNewLeave({...newLeave, daysPerYear: e.target.value})} />
                            <label style={{ display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                                <input type="checkbox" checked={newLeave.isPaid} onChange={e=>setNewLeave({...newLeave, isPaid: e.target.checked})} /> Paid
                            </label>
                            <button className="btn btn-primary" type="submit">Add Leave Type</button>
                        </form>
                    )}
                </div>
            </div>
        </div>
    );
}
