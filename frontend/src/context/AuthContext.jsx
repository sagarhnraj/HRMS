import React, { createContext, useState, useEffect } from 'react';
import axios from 'axios';
import { jwtDecode } from 'jwt-decode';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [activeRole, setActiveRole] = useState(null);
    const [loading, setLoading] = useState(true);

    const getHighestRole = (roles) => {
        if (roles.includes('ADMIN')) return 'ADMIN';
        if (roles.includes('HR')) return 'HR';
        if (roles.includes('MANAGER')) return 'MANAGER';
        if (roles.includes('EMPLOYEE')) return 'EMPLOYEE';
        return roles[0] || null;
    };

    useEffect(() => {
        const initAuth = async () => {
            const token = sessionStorage.getItem('token');
            if (token) {
                try {
                    const decoded = jwtDecode(token);
                    if (decoded.exp * 1000 < Date.now()) {
                        logout();
                    } else {
                        setupAxiosInterceptor(token);
                        // Fetch fresh permissions from /me
                        try {
                            const res = await axios.get(import.meta.env.VITE_API_URL + '/api/auth/me');
                            const { email, roles, permissions } = res.data;
                            setUser({ email, roles, permissions, token });
                            
                            const savedRole = sessionStorage.getItem('activeRole');
                            if (savedRole && roles.includes(savedRole)) {
                                setActiveRole(savedRole);
                            } else {
                                setActiveRole(getHighestRole(roles));
                            }
                        } catch (err) {
                            console.error('Failed to fetch /me', err);
                            // Fallback to JWT payload or session storage if /me fails
                            const roles = JSON.parse(sessionStorage.getItem('roles') || '[]');
                            const permissions = JSON.parse(sessionStorage.getItem('permissions') || '[]');
                            setUser({ email: decoded.sub, roles, permissions, token });
                            setActiveRole(getHighestRole(roles));
                        }
                    }
                } catch (error) {
                    logout();
                }
            }
            setLoading(false);
        };
        initAuth();
    }, []);

    const setupAxiosInterceptor = (token) => {
        axios.interceptors.request.use(config => {
            config.headers.Authorization = `Bearer ${token}`;
            return config;
        });
        axios.interceptors.response.use(response => response, error => {
            if (error.response && error.response.status === 401) {
                logout();
            }
            return Promise.reject(error);
        });
    };

    const login = async (email, password) => {
        try {
            const response = await axios.post(import.meta.env.VITE_API_URL + '/api/auth/login', { email, password });
            const { token, roles, permissions } = response.data;
            sessionStorage.setItem('token', token);
            sessionStorage.setItem('roles', JSON.stringify(roles));
            sessionStorage.setItem('permissions', JSON.stringify(permissions));
            
            setUser({ email, roles, permissions, token });
            const highest = getHighestRole(roles);
            setActiveRole(highest);
            sessionStorage.setItem('activeRole', highest);
            setupAxiosInterceptor(token);
            return true;
        } catch (error) {
            console.error('Login failed', error);
            return false;
        }
    };

    const switchRole = (role) => {
        if (user?.roles.includes(role)) {
            setActiveRole(role);
            sessionStorage.setItem('activeRole', role);
        }
    };

    const logout = async () => {
        if (sessionStorage.getItem('token')) {
            try {
                await axios.post(import.meta.env.VITE_API_URL + '/api/auth/logout', {}, {
                    headers: { Authorization: `Bearer ${sessionStorage.getItem('token')}` }
                });
            } catch (e) {}
        }
        sessionStorage.removeItem('token');
        sessionStorage.removeItem('roles');
        sessionStorage.removeItem('permissions');
        sessionStorage.removeItem('activeRole');
        setUser(null);
        setActiveRole(null);
    };

    return (
        <AuthContext.Provider value={{ user, activeRole, switchRole, login, logout, loading }}>
            {children}
        </AuthContext.Provider>
    );
};
