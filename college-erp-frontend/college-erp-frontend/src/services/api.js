import axios from 'axios';

const BASE = '/api';
const api = axios.create({ baseURL: BASE });

// Attach JWT to every request
api.interceptors.request.use(cfg => {
  const token = localStorage.getItem('accessToken');
  if (token) cfg.headers.Authorization = `Bearer ${token}`;
  return cfg;
});

// Auto-refresh on 401
api.interceptors.response.use(
  res => res,
  async err => {
    const original = err.config;
    if (err.response?.status === 401 && !original._retry) {
      original._retry = true;
      const refresh = localStorage.getItem('refreshToken');
      if (refresh) {
        try {
          const { data } = await axios.post(`${BASE}/auth/refresh-token`, { refreshToken: refresh });
          const newToken = data.data.accessToken;
          localStorage.setItem('accessToken', newToken);
          original.headers.Authorization = `Bearer ${newToken}`;
          return api(original);
        } catch {
          localStorage.clear();
          window.location.href = '/login';
        }
      } else {
        localStorage.clear();
        window.location.href = '/login';
      }
    }
    return Promise.reject(err);
  }
);

// ── Auth ─────────────────────────────────────────────────────────────────
export const authAPI = {
  login:          d => api.post('/auth/login', d),
  logout:         () => api.post('/auth/logout'),
  refreshToken:   d => api.post('/auth/refresh-token', d),
  changePassword: d => api.post('/auth/change-password', d),
  adminResetPassword: d => api.post('/auth/admin/reset-password', d),
  createAdmin:    d => api.post('/auth/admin/create-admin', d),
  setUserActive:  (userId, active) => api.patch(`/auth/admin/users/${userId}/active?active=${active}`),
};

// ── Students ──────────────────────────────────────────────────────────────
export const studentAPI = {
  getAll:      (page=0, size=10, sort='firstName') =>
    api.get(`/students?page=${page}&size=${size}&sort=${sort}`),
  getById:     id  => api.get(`/students/${id}`),
  getByBatch:  bid => api.get(`/students/batch/${bid}`),
  getByDept:   did => api.get(`/students/department/${did}`),
  getByParent: pid => api.get(`/students/parent/${pid}`),
  search:      (q, page=0, size=10) => api.get(`/students/search?q=${encodeURIComponent(q)}&page=${page}&size=${size}`),
  create:      d   => api.post('/students', d),
  update:      (id, d) => api.put(`/students/${id}`, d),
  delete:      id  => api.delete(`/students/${id}`),
  markFaceEnrolled: id => api.patch(`/students/${id}/face-enrolled`),
};

// ── Faculty ───────────────────────────────────────────────────────────────
export const facultyAPI = {
  getAll:           (page=0, size=10) => api.get(`/faculty?page=${page}&size=${size}&sort=firstName`),
  getById:          id  => api.get(`/faculty/${id}`),
  getByDept:        did => api.get(`/faculty/department/${did}`),
  search:           (q, page=0) => api.get(`/faculty/search?q=${encodeURIComponent(q)}&page=${page}&size=10`),
  create:           d   => api.post('/faculty', d),
  update:           (id, d) => api.put(`/faculty/${id}`, d),
  delete:           id  => api.delete(`/faculty/${id}`),
  getAssignments:   fid => api.get(`/faculty/${fid}/assignments`),
  assignSubject:    d   => api.post('/faculty/assignments', d),
  removeAssignment: aid => api.delete(`/faculty/assignments/${aid}`),
};

// ── Courses ───────────────────────────────────────────────────────────────
export const courseAPI = {
  // Departments
  getDepartments:     ()       => api.get('/departments'),
  getDeptById:        id       => api.get(`/departments/${id}`),
  createDept:         d        => api.post('/departments', d),
  updateDept:         (id, d)  => api.put(`/departments/${id}`, d),
  getCoursesByDept:   did      => api.get(`/departments/${did}/courses`),
  getBatchesByDept:   did      => api.get(`/departments/${did}/batches`),
  // Courses
  getCourseById:      id       => api.get(`/courses/${id}`),
  createCourse:       d        => api.post('/courses', d),
  getSubjectsByCourse:(cid, sem) => sem
    ? api.get(`/courses/${cid}/subjects?semester=${sem}`)
    : api.get(`/courses/${cid}/subjects`),
  getBatchesByCourse: cid      => api.get(`/courses/${cid}/batches`),
  // Subjects
  getSubjectById:     id       => api.get(`/subjects/${id}`),
  createSubject:      d        => api.post('/subjects', d),
  // Batches
  getBatchById:       id       => api.get(`/courses/batches/${id}`),
  createBatch:        d        => api.post('/courses/batches', d),
  updateBatchSem:     (bid, sem) => api.patch(`/courses/batches/${bid}/semester?semester=${sem}`),
};

// ── Attendance ────────────────────────────────────────────────────────────
export const attendanceAPI = {
  // field is studentMarks in v2 backend (was studentAttendances)
  markBulk:      d           => api.post('/attendance/bulk', d),
  // face recognition endpoint changed from /face-recognition to /face
  markByFace:    d           => api.post('/attendance/face', d),
  startSession:  d           => api.post('/attendance/session/start', d),
  endSession:    sid         => api.patch(`/attendance/session/${sid}/end`),
  getPercentage: (sid, subid)=> api.get(`/attendance/student/${sid}/subject/${subid}/percentage`),
  getByDate:     (subid, date)=> api.get(`/attendance/subject/${subid}/date/${date}`),
  getByStudent:  sid         => api.get(`/attendance/student/${sid}`),
  getDefaulters: (subid, thr)=> api.get(`/attendance/subject/${subid}/defaulters?threshold=${thr || 75}`),
};

// ── Marks ─────────────────────────────────────────────────────────────────
export const marksAPI = {
  upsert:        d           => api.post('/marks', d),
  bulkUpsert:    d           => api.post('/marks/bulk', d),
  getByStudent:  sid         => api.get(`/marks/student/${sid}`),
  getSemSummary: (sid, sem)  => api.get(`/marks/student/${sid}/semester/${sem}/summary`),
  getBatchMarks: (bid, subid, examType) =>
    api.get(`/marks/batch/${bid}/subject/${subid}?examType=${examType}`),
};

// ── Face Recognition ──────────────────────────────────────────────────────
export const faceAPI = {
  enroll:          (studentId, formData) => api.post(`/face/enroll/${studentId}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  }),
  train:           d   => api.post('/face/train', d),
  recognize:       d   => api.post('/face/recognize', d),
  checkEnrolled:   sid => api.get(`/face/enrolled/${sid}`),
  deleteEnrollment:sid => api.delete(`/face/enroll/${sid}`),
};

// ── Notifications ─────────────────────────────────────────────────────────
export const notifAPI = {
  getMyNotifs:    (page=0) => api.get(`/notifications?page=${page}&size=20`),
  getUnreadCount: ()       => api.get('/notifications/unread-count'),
  markRead:       id       => api.patch(`/notifications/${id}/read`),
  markAllRead:    ()       => api.patch('/notifications/read-all'),
};

// ── Admin ─────────────────────────────────────────────────────────────────
export const adminAPI = {
  getDashboard:  () => api.get('/admin/dashboard'),
  getDefaulters: (subid, thr) =>
    api.get(`/admin/reports/defaulters/subject/${subid}?threshold=${thr || 75}`),
  broadcast:     (title, message, role) =>
    api.post(`/admin/broadcast?title=${encodeURIComponent(title)}&message=${encodeURIComponent(message)}&targetRole=${role}`),
};

// ── Parents ───────────────────────────────────────────────────────────────
export const parentAPI = {
  getAll:  (page=0, size=20) => api.get(`/parents?page=${page}&size=${size}`),
  getMe:   ()        => api.get('/parents/me'),
  getById: id        => api.get(`/parents/${id}`),
  create:  d         => api.post('/parents', d),
  update:  (id, d)   => api.put(`/parents/${id}`, d),
  delete:  id        => api.delete(`/parents/${id}`),
};

export default api;
