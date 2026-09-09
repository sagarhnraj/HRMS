import React, { useContext } from 'react';
import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';

export default function Layout() {
    const { user, activeRole, switchRole, logout } = useContext(AuthContext);
    const navigate = useNavigate();

    if (!user) return null;

    const roleColors = {
        EMPLOYEE: 'role-employee',
        MANAGER: 'role-manager',
        HR: 'role-hr',
        ADMIN: 'role-admin'
    };

    return (
        <div className="layout">
            <aside className="sidebar">
                <div className="sidebar-header">
                    HRMS System
                </div>
                <nav className="sidebar-nav">
                    {activeRole === 'EMPLOYEE' && (
                        <>
                            <NavLink to="/dashboard/employee" end className={({isActive}) => `nav-item ${isActive ? 'active role-employee' : ''}`}>
                                <span>🏠</span> Dashboard
                            </NavLink>
                            <NavLink to="/profile/me" className={({isActive}) => `nav-item ${isActive ? 'active role-employee' : ''}`}>
                                <span>👤</span> My Profile
                            </NavLink>
                        </>
                    )}
                    {activeRole === 'MANAGER' && (
                        <>
                            <NavLink to="/dashboard/manager" end className={({isActive}) => `nav-item ${isActive ? 'active role-manager' : ''}`}>
                                <span>🏠</span> Dashboard
                            </NavLink>
                            <NavLink to="/profile/me" className={({isActive}) => `nav-item ${isActive ? 'active role-manager' : ''}`}>
                                <span>👤</span> My Profile
                            </NavLink>
                        </>
                    )}
                    {activeRole === 'HR' && (
                        <>
                            <NavLink to="/dashboard/hr" end className={({isActive}) => `nav-item ${isActive ? 'active role-hr' : ''}`}>
                                <span>🏠</span> HR Dashboard
                            </NavLink>
                            <NavLink to="/directory" className={({isActive}) => `nav-item ${isActive ? 'active role-hr' : ''}`}>
                                <span>👥</span> Directory
                            </NavLink>
                            <NavLink to="/org" className={({isActive}) => `nav-item ${isActive ? 'active role-hr' : ''}`}>
                                <span>🏢</span> Org Structure
                            </NavLink>
                            <NavLink to="/profile/me" className={({isActive}) => `nav-item ${isActive ? 'active role-hr' : ''}`}>
                                <span>👤</span> My Profile
                            </NavLink>
                        </>
                    )}
                    {activeRole === 'ADMIN' && (
                        <>
                            <NavLink to="/dashboard/admin" end className={({isActive}) => `nav-item ${isActive ? 'active role-admin' : ''}`}>
                                <span>🏠</span> System Admin
                            </NavLink>
                            <NavLink to="/settings/roles" className={({isActive}) => `nav-item ${isActive ? 'active role-admin' : ''}`}>
                                <span>⚙️</span> Roles & Permissions
                            </NavLink>
                            <NavLink to="/settings/users" className={({isActive}) => `nav-item ${isActive ? 'active role-admin' : ''}`}>
                                <span>🔒</span> System Users
                            </NavLink>
                            <NavLink to="/profile/me" className={({isActive}) => `nav-item ${isActive ? 'active role-admin' : ''}`}>
                                <span>👤</span> My Profile
                            </NavLink>
                        </>
                    )}
                </nav>
            </aside>
            <div className="main-content">
                <header className="topbar">
                    <h1 className="page-title">
                        {activeRole === 'EMPLOYEE' && 'Employee Portal'}
                        {activeRole === 'MANAGER' && 'Manager Portal'}
                        {activeRole === 'HR' && 'HR Portal'}
                        {activeRole === 'ADMIN' && 'Admin Portal'}
                    </h1>
                    <div className="user-controls">
                        {user.roles.length > 1 && (
                            <select 
                                className="role-switcher" 
                                value={activeRole} 
                                onChange={(e) => {
                                    switchRole(e.target.value);
                                    navigate(`/dashboard/${e.target.value.toLowerCase()}`);
                                }}
                            >
                                {user.roles.map(r => (
                                    <option key={r} value={r}>Viewing as: {r.charAt(0) + r.slice(1).toLowerCase()}</option>
                                ))}
                            </select>
                        )}
                        <span className={`role-badge ${activeRole.toLowerCase()}`}>{activeRole}</span>
                        <span style={{fontWeight: 500}}>{user.email}</span>
                        <button onClick={() => { logout(); navigate('/login'); }} className="btn btn-outline" style={{padding: '0.4rem 0.8rem'}}>
                            Logout
                        </button>
                    </div>
                </header>
                <div className="dashboard-content">
                    <Outlet />
                </div>
            </div>
        </div>
    );
}
