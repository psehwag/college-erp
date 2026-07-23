import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AuthProvider, useAuth } from './context/AuthContext';
import AppShell from './components/layout/AppShell';

// Auth pages
import LoginPage          from './pages/LoginPage';
import ChangePasswordPage from './pages/ChangePasswordPage';

// Admin pages
import AdminDashboard  from './pages/admin/AdminDashboard';
import StudentsPage    from './pages/admin/StudentsPage';
import FacultyPage     from './pages/admin/FacultyPage';
import ParentsPage     from './pages/admin/ParentsPage';
import AdminsPage      from './pages/admin/AdminsPage';
import CoursesPage     from './pages/admin/CoursesPage';
import FaceEnrollPage  from './pages/admin/FaceEnrollPage';
import DefaultersPage  from './pages/admin/DefaultersPage';

// Faculty pages
import FacultyDashboard from './pages/faculty/FacultyDashboard';
import AttendancePage   from './pages/faculty/AttendancePage';
import MarksPage        from './pages/faculty/MarksPage';

// Student pages
import StudentDashboard     from './pages/student/StudentDashboard';
import StudentAttendancePage from './pages/student/StudentAttendancePage';
import StudentMarksPage      from './pages/student/StudentMarksPage';

// Parent pages — dedicated pages (do NOT reuse student pages: a parent's
// referenceId is the PARENT's id, not any child's id)
import ParentDashboard        from './pages/parent/ParentDashboard';
import ParentAttendancePage   from './pages/parent/ParentAttendancePage';
import ParentMarksPage        from './pages/parent/ParentMarksPage';

// Shared
import NotificationsPage from './pages/NotificationsPage';

function ChangePasswordRoute() {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  return <ChangePasswordPage />;
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Toaster position="top-right" toastOptions={{
          style: { fontFamily:'Inter, sans-serif', fontSize:13.5, borderRadius:8 },
          success: { iconTheme: { primary:'#10B981', secondary:'#fff' } }
        }} />

        <Routes>
          {/* Public */}
          <Route path="/login"          element={<LoginPage />} />
          <Route path="/change-password" element={<ChangePasswordRoute />} />
          <Route path="/"               element={<Navigate to="/login" replace />} />

          {/* Admin */}
          <Route element={<AppShell allowedRoles={['ADMIN']} />}>
            <Route path="/admin"              element={<AdminDashboard />} />
            <Route path="/admin/students"     element={<StudentsPage />} />
            <Route path="/admin/faculty"      element={<FacultyPage />} />
            <Route path="/admin/parents"      element={<ParentsPage />} />
            <Route path="/admin/admins"       element={<AdminsPage />} />
            <Route path="/admin/courses"      element={<CoursesPage />} />
            <Route path="/admin/attendance"   element={<AttendancePage />} />
            <Route path="/admin/marks"        element={<MarksPage />} />
            <Route path="/admin/defaulters"   element={<DefaultersPage />} />
            <Route path="/admin/face"         element={<FaceEnrollPage />} />
          </Route>

          {/* Faculty */}
          <Route element={<AppShell allowedRoles={['FACULTY']} />}>
            <Route path="/faculty"            element={<FacultyDashboard />} />
            <Route path="/faculty/attendance" element={<AttendancePage />} />
            <Route path="/faculty/marks"      element={<MarksPage />} />
          </Route>

          {/* Student */}
          <Route element={<AppShell allowedRoles={['STUDENT']} />}>
            <Route path="/student"            element={<StudentDashboard />} />
            <Route path="/student/attendance" element={<StudentAttendancePage />} />
            <Route path="/student/marks"      element={<StudentMarksPage />} />
          </Route>

          {/* Parent */}
          <Route element={<AppShell allowedRoles={['PARENT']} />}>
            <Route path="/parent"             element={<ParentDashboard />} />
            <Route path="/parent/attendance"  element={<ParentAttendancePage />} />
            <Route path="/parent/marks"       element={<ParentMarksPage />} />
          </Route>

          {/* Notifications — shared across all authenticated roles. Declared
              once here, not duplicated per-role above: React Router resolves
              ties between identically-specific routes by declaration order,
              so four separate "/notifications" entries would always resolve
              to the first (ADMIN) one regardless of the real logged-in role. */}
          <Route element={<AppShell allowedRoles={['ADMIN','FACULTY','STUDENT','PARENT']} />}>
            <Route path="/notifications" element={<NotificationsPage />} />
          </Route>

          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
