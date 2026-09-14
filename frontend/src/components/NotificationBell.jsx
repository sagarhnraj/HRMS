import React, { useState, useEffect, useRef } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

export default function NotificationBell() {
    const [unreadCount, setUnreadCount] = useState(0);
    const [notifications, setNotifications] = useState([]);
    const [isOpen, setIsOpen] = useState(false);
    const dropdownRef = useRef(null);
    const navigate = useNavigate();

    const fetchUnreadCount = async () => {
        try {
            const res = await axios.get(import.meta.env.VITE_API_URL + '/api/notifications/unread-count');
            setUnreadCount(res.data.count);
        } catch (e) {
            console.error('Failed to fetch unread count', e);
        }
    };

    const fetchNotifications = async () => {
        try {
            const res = await axios.get(import.meta.env.VITE_API_URL + '/api/notifications?page=0&size=5');
            setNotifications(res.data.content || []);
        } catch (e) {
            console.error('Failed to fetch notifications', e);
        }
    };

    useEffect(() => {
        fetchUnreadCount();
        const interval = setInterval(fetchUnreadCount, 30000);
        return () => clearInterval(interval);
    }, []);

    useEffect(() => {
        if (isOpen) {
            fetchNotifications();
        }
    }, [isOpen]);

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                setIsOpen(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const markAsRead = async (id) => {
        try {
            await axios.patch(import.meta.env.VITE_API_URL + /api/notifications/ + id + /read);
            fetchUnreadCount();
            fetchNotifications();
        } catch (e) {
            console.error(e);
        }
    };

    const markAllRead = async () => {
        try {
            await axios.patch(import.meta.env.VITE_API_URL + /api/notifications/read-all);
            fetchUnreadCount();
            fetchNotifications();
        } catch (e) {
            console.error(e);
        }
    };

    return (
        <div className="notification-bell" ref={dropdownRef} style={{ position: 'relative', marginRight: '1rem', cursor: 'pointer' }}>
            <div onClick={() => setIsOpen(!isOpen)} style={{ position: 'relative' }}>
                <span style={{ fontSize: '1.2rem' }}>??</span>
                {unreadCount > 0 && (
                    <span className="badge" style={{ position: 'absolute', top: '-5px', right: '-10px', background: 'red', color: 'white', borderRadius: '50%', padding: '2px 6px', fontSize: '0.7rem' }}>
                        {unreadCount}
                    </span>
                )}
            </div>
            
            {isOpen && (
                <div className="notification-dropdown" style={{ position: 'absolute', right: 0, top: '30px', width: '300px', background: 'white', border: '1px solid #ccc', borderRadius: '4px', boxShadow: '0 2px 10px rgba(0,0,0,0.1)', zIndex: 1000, color: 'black' }}>
                    <div style={{ padding: '10px', borderBottom: '1px solid #eee', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <strong style={{color: 'black'}}>Notifications</strong>
                        {unreadCount > 0 && (
                            <button onClick={markAllRead} style={{ fontSize: '0.8rem', background: 'none', border: 'none', color: '#007bff', cursor: 'pointer' }}>Mark all read</button>
                        )}
                    </div>
                    <div style={{ maxHeight: '300px', overflowY: 'auto' }}>
                        {notifications.length === 0 ? (
                            <div style={{ padding: '10px', textAlign: 'center', color: '#777' }}>No notifications</div>
                        ) : (
                            notifications.map(n => (
                                <div key={n.id} style={{ padding: '10px', borderBottom: '1px solid #eee', background: n.read ? 'white' : '#f0f8ff' }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                        <strong>{n.title}</strong>
                                        {!n.read && <span style={{ width: '8px', height: '8px', background: '#007bff', borderRadius: '50%', display: 'inline-block' }}></span>}
                                    </div>
                                    <div style={{ fontSize: '0.85rem', color: '#555', margin: '4px 0' }}>{n.body}</div>
                                    <div style={{ fontSize: '0.75rem', color: '#aaa', display: 'flex', justifyContent: 'space-between' }}>
                                        <span>{new Date(n.createdAt).toLocaleString()}</span>
                                        {!n.read && (
                                            <span onClick={(e) => { e.stopPropagation(); markAsRead(n.id); }} style={{ color: '#007bff', cursor: 'pointer' }}>Mark read</span>
                                        )}
                                    </div>
                                </div>
                            ))
                        )}
                    </div>
                </div>
            )}
        </div>
    );
}
