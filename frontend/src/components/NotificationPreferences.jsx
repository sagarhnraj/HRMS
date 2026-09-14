import React, { useState, useEffect } from 'react';
import axios from 'axios';

export default function NotificationPreferences() {
    const [emailEnabled, setEmailEnabled] = useState(true);
    const [inAppEnabled, setInAppEnabled] = useState(true);

    useEffect(() => {
        const fetchPrefs = async () => {
            try {
                const res = await axios.get(import.meta.env.VITE_API_URL + '/api/notifications/preferences');
                setEmailEnabled(res.data.emailEnabled);
                setInAppEnabled(res.data.inAppEnabled);
            } catch (e) {
                console.error(e);
            }
        };
        fetchPrefs();
    }, []);

    const savePrefs = async (email, inApp) => {
        try {
            await axios.put(import.meta.env.VITE_API_URL + '/api/notifications/preferences', {
                emailEnabled: email,
                inAppEnabled: inApp
            });
        } catch (e) {
            console.error(e);
        }
    };

    return (
        <div className="card">
            <h2 className="card-title">Notification Settings</h2>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                <label style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <input type="checkbox" checked={emailEnabled} onChange={(e) => {
                        setEmailEnabled(e.target.checked);
                        savePrefs(e.target.checked, inAppEnabled);
                    }} />
                    Receive Email Notifications
                </label>
                <label style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <input type="checkbox" checked={inAppEnabled} onChange={(e) => {
                        setInAppEnabled(e.target.checked);
                        savePrefs(emailEnabled, e.target.checked);
                    }} />
                    Receive In-App Notifications
                </label>
            </div>
        </div>
    );
}
