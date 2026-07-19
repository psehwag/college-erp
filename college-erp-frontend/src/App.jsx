import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AuthProvider } from './context/AuthContext';
import AppShell from './components/layout/AppShell';

// Pages
import LoginPage         from './pages/LoginPage';
import AdminDashboard    from './pages/admin/AdminDashboard';
import StudentsPage      from './pages/admin/StudentsPage';
import CoursesPage       from './pages/admin/CoursesPage';
import FaceEnrollPage    from './pages/admin/FaceEnrollPage';
import FacultyDashboard  from './pages/faculty/FacultyDashboard';
import AttendancePage    from './pages/faculty/AttendancePage';
import MarksPage         from './pages/faculty/MarksPage';
import StudentDashboard  from './pages/student/StudentDashboard';
import NotificationsPage from './pages/NotificationsPage';

// Simple placeholder pages
const FacultyPage   = () => <div><h2 style={{ fontFamily:'var(--font-display)', fontSize:21, fontWeight:700, marginBottom:16 }}>Faculty</h2><p style={{ color:'var(--text-muted)' }}>Faculty CRUD — mirrors student management pattern.</p></div>;
const DefaultersPage = () => <div><h2 style={{ fontFamily:'var(--font-display)', fontSize:21, fontWeight:700, marginBottom:16 }}>Defaulter Report</h2><p style={{ color:'var(--text-muted)' }}>Students below 75% attendance are listed here.</p></div>;
const AdminMarks    = () => <MarksPage />;
const AdminAtt      = () => <AttendancePage />;
const StudentAtt    = () => <div><h2 style={{ fontFamily:'var(--font-display)', fontSize:21, fontWeight:700, marginBottom:16 }}>My Attendance</h2><p style={{ color:'var(--text-muted)' }}>Per-subject attendance percentage and history.</p></div>;
const StudentMarks  = () => <div><h2 style={{ fontFamily:'var(--font-display)', fontSize:21, fontWeight:700, marginBottom:16 }}>My Marks</h2><p style={{ color:'var(--text-muted)' }}>All exam results with grade calculation.</p></div>;
const ParentDash    = () => <div><h2 style={{ fontFamily:'var(--font-display)', fontSize:21, fontWeight:700, marginBottom:16 }}>Parent Dashboard</h2><p style={{ color:'var(--text-muted)' }}>View your child's attendance and marks.</p></div>;
const ParentAtt     = () => <div><h2 style={{ fontFamily:'var(--font-display)', fontSize:21, fontWeight:700 }}>Child's Attendance</h2></div>;
const ParentMarks   = () => <div><h2 style={{ fontFamily:'var(--font-display)', fontSize:21, fontWeight:700 }}>Child's Marks</h2></div>;
const MyStudents    = () => <div><h2 style={{ fontFamily:'var(--font-display)', fontSize:21, fontWeight:700 }}>My Students</h2><p style={{ color:'var(--text-muted)', marginTop:8 }}>Students assigned to your batches.</p></div>;

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Toaster position="top-right" toastOptions={{
          style: { fontFamily:'Inter, sans-serif', fontSize:13.5, borderRadius:8 },
          success: { iconTheme: { primary:'#10B981', secondary:'#fff' } }
        }} />

        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/" element={<Navigate to="/login" replace />} />

          {/* Admin routes */}
          <Route element={<AppShell allowedRoles={['ADMIN']} />}>
            <Route path="/admin"             element={<AdminDashboard />} />
            <Route path="/admin/students"    element={<StudentsPage />} />
            <Route path="/admin/faculty"     element={<FacultyPage />} />
            <Route path="/admin/courses"     element={<CoursesPage />} />
            <Route path="/admin/attendance"  element={<AdminAtt />} />
            <Route path="/admin/marks"       element={<AdminMarks />} />
            <Route path="/admin/defaulters"  element={<DefaultersPage />} />
            <Route path="/admin/face"        element={<FaceEnrollPage />} />
            <Route path="/notifications"     element={<NotificationsPage />} />
          </Route>

          {/* Faculty routes */}
          <Route element={<AppShell allowedRoles={['FACULTY']} />}>
            <Route path="/faculty"            element={<FacultyDashboard />} />
            <Route path="/faculty/attendance" element={<AttendancePage />} />
            <Route path="/faculty/marks"      element={<MarksPage />} />
            <Route path="/faculty/students"   element={<MyStudents />} />
            <Route path="/notifications"      element={<NotificationsPage />} />
          </Route>

          {/* Student routes */}
          <Route element={<AppShell allowedRoles={['STUDENT']} />}>
            <Route path="/student"            element={<StudentDashboard />} />
            <Route path="/student/attendance" element={<StudentAtt />} />
            <Route path="/student/marks"      element={<StudentMarks />} />
            <Route path="/notifications"      element={<NotificationsPage />} />
          </Route>

          {/* Parent routes */}
          <Route element={<AppShell allowedRoles={['PARENT']} />}>
            <Route path="/parent"             element={<ParentDash />} />
            <Route path="/parent/attendance"  element={<ParentAtt />} />
            <Route path="/parent/marks"       element={<ParentMarks />} />
            <Route path="/notifications"      element={<NotificationsPage />} />
          </Route>

          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
