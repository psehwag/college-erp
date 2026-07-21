import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import toast from 'react-hot-toast';

const NAV = {
  ADMIN: [
    { label: 'Overview', section: null },
    { label: 'Dashboard',    path: '/admin',           icon: '⊞' },
    { label: 'Management', section: null },
    { label: 'Students',     path: '/admin/students',  icon: '👥' },
    { label: 'Faculty',      path: '/admin/faculty',   icon: '🧑‍🏫' },
    { label: 'Parents',      path: '/admin/parents',   icon: '👨‍👩‍👧' },
    { label: 'Admins',       path: '/admin/admins',    icon: '🛡️' },
    { label: 'Courses',      path: '/admin/courses',   icon: '📚' },
    { label: 'Academic', section: null },
    { label: 'Attendance',   path: '/admin/attendance', icon: '✅' },
    { label: 'Marks',        path: '/admin/marks',     icon: '📊' },
    { label: 'Defaulters',   path: '/admin/defaulters', icon: '⚠️' },
    { label: 'AI Features', section: null },
    { label: 'Face Enroll',  path: '/admin/face',      icon: '🤖' },
    { label: 'Account', section: null },
    { label: 'Notifications',path: '/notifications',   icon: '🔔' },
    { label: 'Change Password',path: '/change-password',icon: '🔐' },
  ],
  FACULTY: [
    { label: 'Overview', section: null },
    { label: 'Dashboard',    path: '/faculty',            icon: '⊞' },
    { label: 'Teaching', section: null },
    { label: 'Attendance',   path: '/faculty/attendance', icon: '✅' },
    { label: 'Marks',        path: '/faculty/marks',      icon: '📊' },
    { label: 'Account', section: null },
    { label: 'Notifications',path: '/notifications',      icon: '🔔' },
    { label: 'Change Password',path: '/change-password',  icon: '🔐' },
  ],
  STUDENT: [
    { label: 'Overview', section: null },
    { label: 'Dashboard',    path: '/student',            icon: '⊞' },
    { label: 'Academic', section: null },
    { label: 'Attendance',   path: '/student/attendance', icon: '✅' },
    { label: 'Marks',        path: '/student/marks',      icon: '📊' },
    { label: 'Account', section: null },
    { label: 'Notifications',path: '/notifications',      icon: '🔔' },
    { label: 'Change Password',path: '/change-password',  icon: '🔐' },
  ],
  PARENT: [
    { label: 'Overview', section: null },
    { label: 'Dashboard',    path: '/parent',             icon: '⊞' },
    { label: "Child's Info", section: null },
    { label: 'Attendance',   path: '/parent/attendance',  icon: '✅' },
    { label: 'Marks',        path: '/parent/marks',       icon: '📊' },
    { label: 'Account', section: null },
    { label: 'Notifications',path: '/notifications',      icon: '🔔' },
    { label: 'Change Password',path: '/change-password',  icon: '🔐' },
  ],
};

export default function Sidebar() {
  const { user, logout } = useAuth();
  const nav = useNavigate();
  const loc = useLocation();

  const items = NAV[user?.role] || [];
  const displayName = user?.name || user?.username || 'User';
  const initials = displayName.split(' ').filter(Boolean).slice(0,2).map(w => w[0]).join('').toUpperCase() || 'U';

  const handleLogout = async () => {
    await logout();
    toast.success('Signed out');
    nav('/login');
  };

  return (
    <aside className="sidebar">
      <div className="sidebar-logo">
        <h1>🎓 ERP Lite</h1>
        <span>Smart College System</span>
      </div>

      <nav className="sidebar-nav">
        {items.map((item, i) => {
          if (item.section !== undefined && item.path === undefined) {
            return item.label
              ? <div key={i} className="nav-section-label">{item.label}</div>
              : null;
          }
          const active = loc.pathname === item.path;
          return (
            <button key={item.path} className={`nav-item ${active ? 'active' : ''}`}
              onClick={() => nav(item.path)}>
              <span className="nav-icon">{item.icon}</span>
              {item.label}
            </button>
          );
        })}
      </nav>

      <div className="sidebar-footer">
        <div className="user-chip">
          <div className="user-avatar">{initials}</div>
          <div className="user-info">
            <div className="user-name">{displayName}</div>
            <div className="user-role">{user?.role?.toLowerCase()}</div>
          </div>
        </div>
        <button className="logout-btn" onClick={handleLogout}>
          <span>↩</span> Sign out
        </button>
      </div>
    </aside>
  );
}
