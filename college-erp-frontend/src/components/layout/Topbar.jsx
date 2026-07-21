import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { notifAPI } from '../../services/api';

const TITLES = {
  '/admin': 'Admin Dashboard', '/admin/students': 'Students', '/admin/faculty': 'Faculty',
  '/admin/parents': 'Parents', '/admin/admins': 'Admin Accounts',
  '/admin/courses': 'Courses & Subjects', '/admin/attendance': 'Attendance',
  '/admin/marks': 'Marks', '/admin/defaulters': 'Defaulter Report',
  '/admin/face': 'Face Enrollment', '/faculty': 'Faculty Dashboard',
  '/faculty/attendance': 'Mark Attendance', '/faculty/marks': 'Upload Marks',
  '/student': 'My Dashboard',
  '/student/attendance': 'My Attendance', '/student/marks': 'My Marks',
  '/parent': 'Parent Dashboard', '/parent/attendance': "Child's Attendance",
  '/parent/marks': "Child's Marks", '/notifications': 'Notifications',
  '/change-password': 'Change Password',
};

export default function Topbar() {
  const loc = useLocation();
  const nav = useNavigate();
  const [unread, setUnread] = useState(0);

  useEffect(() => {
    notifAPI.getUnreadCount()
      .then(r => setUnread(r.data.data || 0))
      .catch(() => {});
  }, [loc.pathname]);

  return (
    <header className="topbar">
      <h1 className="topbar-title">{TITLES[loc.pathname] || 'ERP Lite'}</h1>
      <div className="topbar-actions">
        <button className="notif-btn" onClick={() => nav('/notifications')}>
          🔔
          {unread > 0 && <span className="notif-badge">{unread > 9 ? '9+' : unread}</span>}
        </button>
      </div>
    </header>
  );
}
