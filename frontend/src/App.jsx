import React, { useContext } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, AuthContext } from './context/AuthContext';
import Login from './pages/Login';
import Layout from './layout/Layout';
import EmployeeDashboard from './pages/EmployeeDashboard';
import ManagerDashboard from './pages/ManagerDashboard';
import HrDashboard from './pages/HrDashboard';
import AdminDashboard from './pages/AdminDashboard';
import OrgManagement from './pages/OrgManagement';

const ProtectedRoute = ({ children }) => {
    const { user, loading } = useContext(AuthContext);
    if (loading) return <div>Loading...</div>;
    if (!user) return <Navigate to="/login" replace />;
    return children;
};

const RootRedirect = () => {
    const { activeRole, loading } = useContext(AuthContext);
    if (loading) return <div>Loading...</div>;
    if (activeRole) return <Navigate to={`/dashboard/${activeRole.toLowerCase()}`} replace />;
    return <Navigate to="/login" replace />;
};

export default function App() {
    return (
        <AuthProvider>
            <BrowserRouter>
                <Routes>
                    <Route path="/login" element={<Login />} />
                    
                    <Route path="/" element={<ProtectedRoute><Layout /></ProtectedRoute>}>
                        <Route index element={<RootRedirect />} />
                        <Route path="dashboard/employee" element={<EmployeeDashboard />} />
                        <Route path="dashboard/manager" element={<ManagerDashboard />} />
                        <Route path="dashboard/hr" element={<HrDashboard />} />
                        <Route path="dashboard/admin" element={<AdminDashboard />} />
                        
                        {/* Placeholders for other routes */}
                        <Route path="directory" element={<div style={{padding: '2rem'}}>Employee Directory (Coming soon)</div>} />
                        <Route path="org" element={<OrgManagement />} />
                        <Route path="profile/me" element={<div style={{padding: '2rem'}}>My Profile (Coming soon)</div>} />
                        <Route path="settings/roles" element={<div style={{padding: '2rem'}}>Roles & Permissions Matrix (Coming soon)</div>} />
                        <Route path="settings/users" element={<div style={{padding: '2rem'}}>System Users (Coming soon)</div>} />
                    </Route>
                    
                    <Route path="*" element={<Navigate to="/" replace />} />
                </Routes>
            </BrowserRouter>
        </AuthProvider>
    );
}
