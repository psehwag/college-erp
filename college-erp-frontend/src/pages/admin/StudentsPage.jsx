import React, { useEffect, useState, useCallback } from 'react';
import { studentAPI, courseAPI } from '../../services/api';
import toast from 'react-hot-toast';

function StudentModal({ student, depts, onClose, onSaved }) {
  const [form, setForm] = useState(student || {
    firstName:'', lastName:'', email:'', phone:'',
    departmentId:'', courseId:'', batchId:'', currentSemester:1, admissionYear: new Date().getFullYear()
  });
  const [courses, setCourses] = useState([]);
  const [batches, setBatches] = useState([]);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (form.departmentId) {
      courseAPI.getCoursesByDept(form.departmentId).then(r => setCourses(r.data.data || []));
      courseAPI.getBatchesByDept(form.departmentId).then(r => setBatches(r.data.data || []));
    }
  }, [form.departmentId]);

  const set = (k, v) => setForm(f => ({ ...f, [k]: v }));

  const submit = async e => {
    e.preventDefault();
    setSaving(true);
    try {
      if (student?.id) await studentAPI.update(student.id, form);
      else await studentAPI.create(form);
      toast.success(student ? 'Student updated' : 'Student added');
      onSaved();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to save');
    } finally { setSaving(false); }
  };

  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
      <div className="modal modal-lg">
        <div className="modal-header">
          <h3>{student ? 'Edit student' : 'Add student'}</h3>
          <button className="modal-close" onClick={onClose}>✕</button>
        </div>
        <form onSubmit={submit}>
          <div className="modal-body">
            <div className="form-grid form-grid-2" style={{ gap:14 }}>
              {[['firstName','First name'],['lastName','Last name'],['email','Email'],['phone','Phone']].map(([k,l]) => (
                <div className="form-group" key={k}>
                  <label className="form-label">{l}</label>
                  <input className="form-input" value={form[k]||''} onChange={e => set(k, e.target.value)}
                    required={['firstName','lastName','email'].includes(k)} />
                </div>
              ))}
              <div className="form-group">
                <label className="form-label">Department</label>
                <select className="form-select" value={form.departmentId||''} onChange={e => set('departmentId', e.target.value)} required>
                  <option value="">Select department</option>
                  {depts.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Course</label>
                <select className="form-select" value={form.courseId||''} onChange={e => set('courseId', e.target.value)} required>
                  <option value="">Select course</option>
                  {courses.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Batch</label>
                <select className="form-select" value={form.batchId||''} onChange={e => set('batchId', e.target.value)}>
                  <option value="">Select batch</option>
                  {batches.map(b => <option key={b.id} value={b.id}>{b.name}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Semester</label>
                <select className="form-select" value={form.currentSemester||1} onChange={e => set('currentSemester', +e.target.value)}>
                  {[1,2,3,4,5,6,7,8].map(s => <option key={s} value={s}>Semester {s}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Admission year</label>
                <input className="form-input" type="number" min="2000" max="2030" value={form.admissionYear||''}
                  onChange={e => set('admissionYear', +e.target.value)} required />
              </div>
            </div>
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? <span className="spinner" /> : (student ? 'Save changes' : 'Add student')}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default function StudentsPage() {
  const [students, setStudents] = useState([]);
  const [depts, setDepts]       = useState([]);
  const [total, setTotal]       = useState(0);
  const [page, setPage]         = useState(0);
  const [query, setQuery]       = useState('');
  const [modal, setModal]       = useState(null); // null | 'add' | student obj
  const [loading, setLoading]   = useState(true);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = query
        ? await studentAPI.search(query, page)
        : await studentAPI.getAll(page);
      setStudents(res.data.data?.content || res.data.data || []);
      setTotal(res.data.total || res.data.data?.totalElements || 0);
    } catch { toast.error('Failed to load students'); }
    setLoading(false);
  }, [page, query]);

  useEffect(() => { courseAPI.getDepartments().then(r => setDepts(r.data.data || [])); }, []);
  useEffect(() => { load(); }, [load]);

  const deactivate = async id => {
    if (!window.confirm('Deactivate this student?')) return;
    try { await studentAPI.delete(id); toast.success('Student deactivated'); load(); }
    catch { toast.error('Failed'); }
  };

  return (
    <div>
      <div className="page-header">
        <div className="page-header-left">
          <h2>Students</h2>
          <p>{total} total students</p>
        </div>
        <div className="page-actions">
          <button className="btn btn-primary" onClick={() => setModal('add')}>+ Add student</button>
        </div>
      </div>

      {/* Search */}
      <div style={{ marginBottom:16 }}>
        <div className="search-bar" style={{ maxWidth:360 }}>
          <span className="search-icon">🔍</span>
          <input placeholder="Search by name, email, enrollment…"
            value={query} onChange={e => { setQuery(e.target.value); setPage(0); }} />
        </div>
      </div>

      <div className="table-wrap">
        {loading ? (
          <div style={{ textAlign:'center', padding:40 }}><div className="spinner spinner-dark" style={{ margin:'0 auto' }} /></div>
        ) : students.length === 0 ? (
          <div className="empty-state"><div className="empty-icon">👥</div><p>No students found</p></div>
        ) : (
          <table>
            <thead><tr>
              <th>Enrollment</th><th>Name</th><th>Email</th><th>Semester</th><th>Status</th><th>Face</th><th></th>
            </tr></thead>
            <tbody>
              {students.map(s => (
                <tr key={s.id}>
                  <td><span className="badge badge-gray">{s.enrollmentNumber}</span></td>
                  <td style={{ fontWeight:500 }}>{s.fullName || `${s.firstName} ${s.lastName}`}</td>
                  <td className="text-muted text-sm">{s.email}</td>
                  <td>Sem {s.currentSemester}</td>
                  <td><span className={`badge ${s.status==='ACTIVE'?'badge-green':'badge-gray'}`}>{s.status}</span></td>
                  <td><span className={`badge ${s.faceEnrolled?'badge-blue':'badge-amber'}`}>
                    {s.faceEnrolled ? '✓ Enrolled' : 'Pending'}
                  </span></td>
                  <td>
                    <div style={{ display:'flex', gap:6 }}>
                      <button className="btn btn-ghost btn-sm" onClick={() => setModal(s)}>Edit</button>
                      <button className="btn btn-ghost btn-sm" style={{ color:'var(--danger)' }}
                        onClick={() => deactivate(s.id)}>Remove</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Pagination */}
      <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginTop:14 }}>
        <span className="text-sm text-muted">Showing {students.length} of {total}</span>
        <div style={{ display:'flex', gap:6 }}>
          <button className="btn btn-secondary btn-sm" disabled={page===0} onClick={() => setPage(p=>p-1)}>← Prev</button>
          <button className="btn btn-secondary btn-sm" disabled={students.length<10} onClick={() => setPage(p=>p+1)}>Next →</button>
        </div>
      </div>

      {modal && (
        <StudentModal
          student={modal === 'add' ? null : modal}
          depts={depts}
          onClose={() => setModal(null)}
          onSaved={() => { setModal(null); load(); }}
        />
      )}
    </div>
  );
}
