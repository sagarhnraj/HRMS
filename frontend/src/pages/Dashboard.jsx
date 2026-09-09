import React, { useContext } from 'react';
import { AuthContext } from '../context/AuthContext';

export default function Dashboard() {
    const { user, logout } = useContext(AuthContext);
    
    if (!user) return null;

    const availableActions = [
        { id: 'PROFILE_VIEW_SELF', title: 'My Profile', desc: 'View and edit your personal details', icon: '👤' },
        { id: 'LEAVE_APPLY', title: 'Apply for Leave', desc: 'Submit a new time-off request', icon: '🏖️' },
        { id: 'LEAVE_REVIEW_TEAM', title: 'Team Leaves', desc: 'Approve or reject team requests', icon: '✅' },
        { id: 'EMPLOYEE_CREATE', title: 'Manage Employees', desc: 'Add or update employee records', icon: '👥' },
        { id: 'ROLE_MANAGE', title: 'System Settings', desc: 'Configure roles and system access', icon: '⚙️' },
    ];

    const userActions = availableActions.filter(action => 
        user.permissions.includes(action.id)
    );

    return (
        <div className="dashboard-layout">
            <header className="dashboard-header">
                <div className="dashboard-brand">HRMS</div>
                <div className="user-profile">
                    <div className="user-info">
                        <span className="user-email">{user.email}</span>
                        <span className="user-roles">{user.roles.join(', ')}</span>
                    </div>
                    <button onClick={logout} className="btn btn-outline" style={{ padding: '0.5rem 1rem' }}>
                        Logout
                    </button>
                </div>
            </header>

            <main className="dashboard-main">
                <div className="welcome-banner">
                    <h1 className="welcome-title">Welcome back!</h1>
                    <p className="welcome-subtitle">Here is an overview of your available actions based on your role.</p>
                </div>

                <div className="actions-grid">
                    {userActions.map(action => (
                        <div key={action.id} className="action-card">
                            <div className="action-icon">{action.icon}</div>
                            <div className="action-title">{action.title}</div>
                            <div className="action-desc">{action.desc}</div>
                        </div>
                    ))}
                    
                    {userActions.length === 0 && (
                        <div style={{ color: 'var(--text-muted)' }}>No actions available for your role.</div>
                    )}
                </div>
            </main>
        </div>
    );
}
