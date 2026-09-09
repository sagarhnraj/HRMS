import React from 'react';
export default function AdminDashboard() {
    return (
        <div>
            <div className="kpi-grid">
                <div className="kpi-card role-admin">
                    <div className="kpi-title">System Users</div>
                    <div className="kpi-value">12</div>
                </div>
                <div className="kpi-card role-admin">
                    <div className="kpi-title">Active Roles</div>
                    <div className="kpi-value">4</div>
                </div>
            </div>
            <h2 style={{marginTop: '2.5rem', marginBottom: '1.5rem'}}>Quick Links</h2>
            <div style={{display: 'flex', gap: '1rem'}}>
                <button className="btn btn-primary">Manage Roles</button>
                <button className="btn btn-outline">System Audit Logs</button>
                <button className="btn btn-outline">Global Settings</button>
            </div>
        </div>
    );
}
