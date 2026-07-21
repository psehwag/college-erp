import React, { useEffect, useState, useCallback } from 'react';
import { studentAPI, courseAPI } from '../../services/api';
import toast from 'react-hot-toast';

// ── Credential banner shown after creating a student ─────────────────────
function CredentialBanner({ data, onClose }) {
  if (!data) return null;
  return (
    <div style={{ padding:'16px 20px', background:'#D1FAE5', border:'1px solid #6EE7B7',
      borderRadius:10, marginBottom:20, display:'flex', justifyContent:'space-between',
      alignItems:'flex-start', gap:12 }}>
      <div>
        <p style={{ fontWeight:600, fontSize:14, color:'#065f46', marginBottom:4 }}>
          ✅ Student created — login credentials generated
        </p>
        <p style={{ fontSize:13, color:'#065f46' }}>
          <strong>Username:</strong> {data.loginUsername} &nbsp;·&nbsp;
          <strong>Password:</strong> Password@123 &nbsp;·&nbsp;
          <strong>Must change password on first login</strong>
        </p>
      </div>
      <button onClick={onClose} style={{ background:'none', border:'none', cursor:'pointer',
        fontSize:18, color:'#065f46', flexShrink:0 }}>✕</button>
    </div>
  );
}

// ── Student modal — every field editable, incl. active/inactive checkbox ──
function StudentModal({ student, depts, onClose, onSaved }) {
  const [form, setForm] = useState(student ? {
    firstName: student.firstName, lastName: student.lastName,
    email: student.email, phone: student.phone || '',
    dateOfBirth: student.dateOfBirth || '', gender: student.gender || '',
    address: student.address || '',
    departmentId: student.departmentId, courseId: student.courseId,
    batchId: student.batchId || '', currentSemester: student.currentSemester,
    admissionYear: student.admissionYear, parentId: student.parentId ?? '',
    isActive: student.status === 'ACTIVE',
  } : {
    firstName:'', lastName:'', email:'', phone:'', dateOfBirth:'', gender:'', address:'',
    departmentId:'', courseId:'', batchId:'',
    currentSemester:1, admissionYear: new Date().getFullYear(), parentId:'', isActive: true,
  });
  const [courses, setCourses] = useState([]);
  const [batches, setBatches] = useState([]);
  const [saving, setSaving]   = useState(false);

  useEffect(() => {
    if (form.departmentId) {
      courseAPI.getCoursesByDept(form.departmentId).then(r => setCourses(r.data.data || [])).catch(()=>{});
      courseAPI.getBatchesByDept(form.departmentId).then(r => setBatches(r.data.data || [])).catch(()=>{});
    }
  }, [form.departmentId]);

  const set = (k, v) => setForm(f => ({ ...f, [k]: v }));

  const submit = async e => {
    e.preventDefault();
    setSaving(true);
    try {
      if (student?.id) {
        const payload = {
          firstName: form.firstName, lastName: form.lastName, email: form.email,
          phone: form.phone, dateOfBirth: form.dateOfBirth || null, gender: form.gender || null,
          address: form.address, departmentId: +form.departmentId, courseId: +form.courseId,
          batchId: form.batchId ? +form.batchId : null, currentSemester: +form.currentSemester,
          admissionYear: +form.admissionYear,
          parentId: form.parentId === '' ? null : +form.parentId,
          status: form.isActive ? 'ACTIVE' : 'INACTIVE',
        };
        await studentAPI.update(student.id, payload);
        toast.success('Student updated');
        onSaved(null);
      } else {
        const result = await studentAPI.create(form);
        onSaved(result.data.data);
      }
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to save student');
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
            {!student && (
              <div style={{ padding:'10px 14px', background:'var(--accent-soft)',
                borderRadius:8, marginBottom:16, fontSize:13, color:'var(--accent)' }}>
                ℹ Login credentials will be auto-generated after saving.
              </div>
            )}
            <div className="form-grid form-grid-2" style={{ gap:14 }}>
              {[['firstName','First name',true,'text'],['lastName','Last name',true,'text'],
                ['email','Email',true,'email'],['phone','Phone',false,'text'],
                ['dateOfBirth','Date of birth',false,'date']].map(([k,l,req,type]) => (
                <div className="form-group" key={k}>
                  <label className="form-label">{l}</label>
                  <input className="form-input" value={form[k]||''}
                    onChange={e => set(k, e.target.value)}
                    required={req} type={type} />
                </div>
              ))}
              <div className="form-group">
                <label className="form-label">Gender</label>
                <select className="form-select" value={form.gender||''} onChange={e => set('gender', e.target.value)}>
                  <option value="">Not specified</option>
                  <option value="MALE">Male</option>
                  <option value="FEMALE">Female</option>
                  <option value="OTHER">Other</option>
                </select>
              </div>
              <div className="form-group" style={{ gridColumn: '1/-1' }}>
                <label className="form-label">Address</label>
                <input className="form-input" value={form.address||''}
                  onChange={e => set('address', e.target.value)} />
              </div>
              <div className="form-group">
                <label className="form-label">Department</label>
                <select className="form-select" value={form.departmentId||''}
                  onChange={e => set('departmentId', e.target.value)} required>
                  <option value="">Select department</option>
                  {depts.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Course</label>
                <select className="form-select" value={form.courseId||''}
                  onChange={e => set('courseId', e.target.value)} required>
                  <option value="">Select course</option>
                  {courses.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Batch</label>
                <select className="form-select" value={form.batchId||''}
                  onChange={e => set('batchId', e.target.value)}>
                  <option value="">Select batch</option>
                  {batches.map(b => <option key={b.id} value={b.id}>{b.name}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Semester</label>
                <select className="form-select" value={form.currentSemester||1}
                  onChange={e => set('currentSemester', +e.target.value)}>
                  {[1,2,3,4,5,6,7,8].map(s => <option key={s} value={s}>Semester {s}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Admission year</label>
                <input className="form-input" type="number" min="2000" max="2030"
                  value={form.admissionYear||''} required
                  onChange={e => set('admissionYear', +e.target.value)} />
              </div>
              <div className="form-group">
                <label className="form-label">Parent ID (optional)</label>
                <input className="form-input" type="number" placeholder="Leave blank if none"
                  value={form.parentId} onChange={e => set('parentId', e.target.value)} />
              </div>

              {student && (
                <div className="form-group" style={{ gridColumn:'1/-1' }}>
                  <label style={{ display:'flex', alignItems:'center', gap:10, cursor:'pointer', fontSize:13.5 }}>
                    <input type="checkbox" checked={form.isActive}
                      onChange={e => set('isActive', e.target.checked)}
                      style={{ width:16, height:16 }} />
                    <span>Account is <strong>{form.isActive ? 'Active' : 'Inactive'}</strong>
                      {' '}— unchecking this also deactivates their login</span>
                  </label>
                </div>
              )}
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

// ── Main page ─────────────────────────────────────────────────────────────
export default function StudentsPage() {
  const [students, setStudents] = useState([]);
  const [depts, setDepts]       = useState([]);
  const [total, setTotal]       = useState(0);
  const [page, setPage]         = useState(0);
  const [query, setQuery]       = useState('');
  const [modal, setModal]       = useState(null);
  const [loading, setLoading]   = useState(true);
  const [newCred, setNewCred]   = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = query
        ? await studentAPI.search(query, page)
        : await studentAPI.getAll(page);
      const d = res.data;
      const list = d.data?.content ?? d.data ?? [];
      setStudents(list);
      setTotal(d.total ?? d.data?.totalElements ?? list.length);
    } catch { toast.error('Failed to load students'); }
    setLoading(false);
  }, [page, query]);

  useEffect(() => {
    courseAPI.getDepartments().then(r => setDepts(r.data.data || [])).catch(()=>{});
  }, []);

  useEffect(() => { load(); }, [load]);

  // Always re-fetch from the server after any save so the table reflects
  // exactly what's in the database (fixes the "screen doesn't update" bug).
  const handleSaved = created => {
    setModal(null);
    if (created) setNewCred(created);
    load();
  };

  const remove = async id => {
    if (!window.confirm(
      'This will PERMANENTLY delete this student along with their attendance history, ' +
      'marks, face enrollment data, and login account. This cannot be undone. Continue?'
    )) return;
    try {
      await studentAPI.delete(id);
      toast.success('Student and all dependent data permanently deleted');
      load();
    } catch (err) { toast.error(err.response?.data?.message || 'Failed to delete'); }
  };

  return (
    <div>
      <div className="page-header">
        <div className="page-header-left">
          <h2>Students</h2>
          <p>{total} total students</p>
        </div>
        <div className="page-actions">
          <button className="btn btn-primary" onClick={() => setModal('add')}>
            + Add student
          </button>
        </div>
      </div>

      <CredentialBanner data={newCred} onClose={() => setNewCred(null)} />

      <div style={{ marginBottom:16 }}>
        <div className="search-bar" style={{ maxWidth:380 }}>
          <span className="search-icon">🔍</span>
          <input placeholder="Search name, email, enrollment…"
            value={query}
            onChange={e => { setQuery(e.target.value); setPage(0); }} />
          {query && (
            <button style={{ background:'none', border:'none', cursor:'pointer',
              color:'var(--text-muted)', fontSize:14 }}
              onClick={() => { setQuery(''); setPage(0); }}>✕</button>
          )}
        </div>
      </div>

      <div className="table-wrap">
        {loading ? (
          <div style={{ textAlign:'center', padding:40 }}>
            <div className="spinner spinner-dark" style={{ margin:'0 auto' }} />
          </div>
        ) : students.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">👥</div>
            <p>{query ? 'No students match your search' : 'No students yet. Add one to get started.'}</p>
          </div>
        ) : (
          <table>
            <thead><tr>
              <th>Enrollment</th><th>Name</th><th>Email</th>
              <th>Login</th><th>Semester</th><th>Status</th><th>Face</th><th></th>
            </tr></thead>
            <tbody>
              {students.map(s => (
                <tr key={s.id}>
                  <td><span className="badge badge-gray">{s.enrollmentNumber}</span></td>
                  <td style={{ fontWeight:500 }}>
                    {s.fullName || `${s.firstName} ${s.lastName}`}
                  </td>
                  <td className="text-muted text-sm">{s.email}</td>
                  <td>
                    {s.loginUsername
                      ? <span className="badge badge-blue">{s.loginUsername}</span>
                      : <span className="text-muted text-sm">—</span>}
                  </td>
                  <td>Sem {s.currentSemester}</td>
                  <td>
                    <span className={`badge ${s.status==='ACTIVE'?'badge-green':'badge-gray'}`}>
                      {s.status}
                    </span>
                  </td>
                  <td>
                    <span className={`badge ${s.faceEnrolled?'badge-blue':'badge-amber'}`}>
                      {s.faceEnrolled ? '✓ Enrolled' : 'Pending'}
                    </span>
                  </td>
                  <td>
                    <div style={{ display:'flex', gap:6 }}>
                      <button className="btn btn-ghost btn-sm"
                        onClick={() => setModal(s)}>Edit</button>
                      <button className="btn btn-ghost btn-sm"
                        style={{ color:'var(--danger)' }}
                        onClick={() => remove(s.id)}>Remove</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginTop:14 }}>
        <span className="text-sm text-muted">Showing {students.length} of {total}</span>
        <div style={{ display:'flex', gap:6 }}>
          <button className="btn btn-secondary btn-sm" disabled={page===0}
            onClick={() => setPage(p=>p-1)}>← Prev</button>
          <button className="btn btn-secondary btn-sm" disabled={students.length<10}
            onClick={() => setPage(p=>p+1)}>Next →</button>
        </div>
      </div>

      {modal && (
        <StudentModal
          student={modal === 'add' ? null : modal}
          depts={depts}
          onClose={() => setModal(null)}
          onSaved={handleSaved}
        />
      )}
    </div>
  );
}
