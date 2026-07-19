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
    if (err.response?.status === 401) {
      const refresh = localStorage.getItem('refreshToken');
      if (refresh) {
        try {
          const { data } = await axios.post(`${BASE}/auth/refresh-token`, { refreshToken: refresh });
          localStorage.setItem('accessToken', data.data.accessToken);
          err.config.headers.Authorization = `Bearer ${data.data.accessToken}`;
          return api(err.config);
        } catch {
          localStorage.clear();
          window.location.href = '/login';
        }
      }
    }
    return Promise.reject(err);
  }
);

// ── Auth ────────────────────────────────────────────────────────────────
export const authAPI = {
  login:          (data) => api.post('/auth/login', data),
  register:       (data) => api.post('/auth/register', data),
  changePassword: (data) => api.post('/auth/change-password', data),
  logout:         ()     => api.post('/auth/logout'),
};

// ── Students ────────────────────────────────────────────────────────────
export const studentAPI = {
  getAll:       (page=0, size=10, sort='firstName') => api.get(`/students?page=${page}&size=${size}&sort=${sort}`),
  getById:      (id)   => api.get(`/students/${id}`),
  getByBatch:   (bid)  => api.get(`/students/batch/${bid}`),
  getByDept:    (did)  => api.get(`/students/department/${did}`),
  search:       (q, p=0, s=10) => api.get(`/students/search?q=${q}&page=${p}&size=${s}`),
  create:       (data) => api.post('/students', data),
  update:       (id, data) => api.put(`/students/${id}`, data),
  delete:       (id)   => api.delete(`/students/${id}`),
};

// ── Faculty ─────────────────────────────────────────────────────────────
export const facultyAPI = {
  getAll:          (page=0, size=10) => api.get(`/faculty?page=${page}&size=${size}&sort=firstName`),
  getById:         (id)   => api.get(`/faculty/${id}`),
  getByDept:       (did)  => api.get(`/faculty/department/${did}`),
  search:          (q, p=0) => api.get(`/faculty/search?q=${q}&page=${p}&size=10`),
  create:          (data) => api.post('/faculty', data),
  update:          (id, data) => api.put(`/faculty/${id}`, data),
  delete:          (id)   => api.delete(`/faculty/${id}`),
  getAssignments:  (fid)  => api.get(`/faculty/${fid}/assignments`),
  assignSubject:   (data) => api.post('/faculty/assignments', data),
  removeAssignment:(aid)  => api.delete(`/faculty/assignments/${aid}`),
};

// ── Courses ─────────────────────────────────────────────────────────────
export const courseAPI = {
  getDepartments:  ()    => api.get('/departments'),
  getDeptById:     (id)  => api.get(`/departments/${id}`),
  createDept:      (d)   => api.post('/departments', d),
  updateDept:      (id,d)=> api.put(`/departments/${id}`, d),

  getCoursesByDept:(did) => api.get(`/departments/${did}/courses`),
  getCourseById:   (id)  => api.get(`/courses/${id}`),
  createCourse:    (d)   => api.post('/courses', d),

  getSubjectsByCourse: (cid, sem) => sem
    ? api.get(`/courses/${cid}/subjects?semester=${sem}`)
    : api.get(`/courses/${cid}/subjects`),
  getSubjectById:  (id)  => api.get(`/subjects/${id}`),
  createSubject:   (d)   => api.post('/subjects', d),

  getBatchesByCourse:(cid) => api.get(`/courses/${cid}/batches`),
  getBatchesByDept:(did) => api.get(`/departments/${did}/batches`),
  getBatchById:    (id)  => api.get(`/courses/batches/${id}`),
  createBatch:     (d)   => api.post('/courses/batches', d),
  updateBatchSem:  (bid, sem) => api.patch(`/courses/batches/${bid}/semester?semester=${sem}`),
};

// ── Attendance ──────────────────────────────────────────────────────────
export const attendanceAPI = {
  markBulk:         (data)       => api.post('/attendance/bulk', data),
  markByFace:       (data)       => api.post('/attendance/face-recognition', data),
  startSession:     (data)       => api.post('/attendance/session/start', data),
  endSession:       (sid)        => api.patch(`/attendance/session/${sid}/end`),
  getPercentage:    (sid, subid) => api.get(`/attendance/student/${sid}/subject/${subid}/percentage`),
  getByDate:        (subid, date)=> api.get(`/attendance/subject/${subid}/date/${date}`),
  getDefaulters:    (subid, thr) => api.get(`/attendance/subject/${subid}/defaulters?threshold=${thr||75}`),
};

// ── Marks ───────────────────────────────────────────────────────────────
export const marksAPI = {
  upsert:         (data)       => api.post('/marks', data),
  bulkUpsert:     (data)       => api.post('/marks/bulk', data),
  getByStudent:   (sid)        => api.get(`/marks/student/${sid}`),
  getSemSummary:  (sid, sem)   => api.get(`/marks/student/${sid}/semester/${sem}/summary`),
  getBatchMarks:  (bid, subid, examType) => api.get(`/marks/batch/${bid}/subject/${subid}?examType=${examType}`),
};

// ── Face Recognition ────────────────────────────────────────────────────
export const faceAPI = {
  enroll:     (studentId, formData) => api.post(`/face/enroll/${studentId}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }),
  train:      (data) => api.post('/face/train', data),
  recognize:  (data) => api.post('/face/recognize', data),
  checkEnrolled: (sid) => api.get(`/face/enrolled/${sid}`),
  deleteEnrollment: (sid) => api.delete(`/face/enroll/${sid}`),
};

// ── Notifications ───────────────────────────────────────────────────────
export const notifAPI = {
  getMyNotifs:    (page=0) => api.get(`/notifications?page=${page}&size=20`),
  getUnreadCount: ()       => api.get('/notifications/unread-count'),
  markRead:       (id)     => api.patch(`/notifications/${id}/read`),
  markAllRead:    ()       => api.patch('/notifications/read-all'),
  send:           (data)   => api.post('/notifications/send', data),
};

// ── Admin ───────────────────────────────────────────────────────────────
export const adminAPI = {
  getDashboard:        ()       => api.get('/admin/dashboard'),
  getFacultyDashboard: (fid)    => api.get(`/admin/dashboard/faculty/${fid}`),
  getStudentDashboard: (sid)    => api.get(`/admin/dashboard/student/${sid}`),
  getDefaulters:       (subid, thr) => api.get(`/admin/reports/defaulters/subject/${subid}?threshold=${thr||75}`),
  broadcast:           (title, message, role) =>
    api.post(`/admin/broadcast?title=${encodeURIComponent(title)}&message=${encodeURIComponent(message)}&targetRole=${role}`),
};

// ── Parents ─────────────────────────────────────────────────────────────
export const parentAPI = {
  getMe:   ()     => api.get('/parents/me'),
  getById: (id)   => api.get(`/parents/${id}`),
  create:  (data) => api.post('/parents', data),
  update:  (id, data) => api.put(`/parents/${id}`, data),
};

export default api;
