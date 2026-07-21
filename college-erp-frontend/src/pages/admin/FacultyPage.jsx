import React, { useEffect, useState, useCallback } from 'react';
import { facultyAPI, courseAPI } from '../../services/api';
import AcademicCascadeSelect from '../../components/common/AcademicCascadeSelect';
import toast from 'react-hot-toast';

function CredentialBanner({ data, onClose }) {
  if (!data) return null;
  return (
    <div style={{ padding:'16px 20px', background:'#D1FAE5', border:'1px solid #6EE7B7',
      borderRadius:10, marginBottom:20, display:'flex', justifyContent:'space-between',
      alignItems:'flex-start', gap:12 }}>
      <div>
        <p style={{ fontWeight:600, fontSize:14, color:'#065f46', marginBottom:4 }}>
          ✅ Faculty created — login credentials generated
        </p>
        <p style={{ fontSize:13, color:'#065f46' }}>
          <strong>Username:</strong> {data.loginUsername} &nbsp;·&nbsp;
          <strong>Password:</strong> Password@123 &nbsp;·&nbsp;
          <strong>Must change password on first login</strong>
        </p>
      </div>
      <button onClick={onClose} style={{ background:'none', border:'none', cursor:'pointer',
        fontSize:18, color:'#065f46' }}>✕</button>
    </div>
  );
}

function FacultyModal({ faculty, depts, onClose, onSaved }) {
  const [form, setForm] = useState(faculty ? {
    firstName: faculty.firstName, lastName: faculty.lastName,
    email: faculty.email, phone: faculty.phone || '',
    departmentId: faculty.departmentId, designation: faculty.designation || '',
    qualification: faculty.qualification || '', specialization: faculty.specialization || '',
    experienceYears: faculty.experienceYears || '',
    isActive: faculty.status === 'ACTIVE',
  } : {
    firstName:'', lastName:'', email:'', phone:'',
    departmentId:'', designation:'', qualification:'', specialization:'', experienceYears:'',
    isActive: true,
  });
  const [saving, setSaving] = useState(false);
  const set = (k, v) => setForm(f => ({ ...f, [k]: v }));

  const submit = async e => {
    e.preventDefault();
    setSaving(true);
    try {
      if (faculty?.id) {
        const payload = {
          firstName: form.firstName, lastName: form.lastName, email: form.email,
          phone: form.phone, departmentId: +form.departmentId, designation: form.designation,
          qualification: form.qualification, specialization: form.specialization,
          experienceYears: form.experienceYears === '' ? null : +form.experienceYears,
          status: form.isActive ? 'ACTIVE' : 'INACTIVE',
        };
        await facultyAPI.update(faculty.id, payload);
        toast.success('Faculty updated');
        onSaved(null);
      } else {
        const res = await facultyAPI.create(form);
        onSaved(res.data.data);
      }
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to save');
    } finally { setSaving(false); }
  };

  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
      <div className="modal modal-lg">
        <div className="modal-header">
          <h3>{faculty ? 'Edit faculty' : 'Add faculty'}</h3>
          <button className="modal-close" onClick={onClose}>✕</button>
        </div>
        <form onSubmit={submit}>
          <div className="modal-body">
            {!faculty && (
              <div style={{ padding:'10px 14px', background:'var(--accent-soft)',
                borderRadius:8, marginBottom:16, fontSize:13, color:'var(--accent)' }}>
                ℹ Login credentials will be auto-generated after saving.
              </div>
            )}
            <div className="form-grid form-grid-2" style={{ gap:14 }}>
              {[['firstName','First name',true],['lastName','Last name',true],
                ['email','Email',true],['phone','Phone',false],
                ['designation','Designation',false],['qualification','Qualification',false],
                ['specialization','Specialization',false]].map(([k,l,req]) => (
                <div className="form-group" key={k}>
                  <label className="form-label">{l}</label>
                  <input className="form-input" value={form[k]||''}
                    onChange={e => set(k, e.target.value)}
                    required={req} type={k==='email'?'email':'text'} />
                </div>
              ))}
              <div className="form-group">
                <label className="form-label">Department</label>
                <select className="form-select" value={form.departmentId||''}
                  onChange={e => set('departmentId', e.target.value)} required>
                  <option value="">Select department</option>
                  {depts.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Experience (years)</label>
                <input className="form-input" type="number" min="0" max="50"
                  value={form.experienceYears||''}
                  onChange={e => set('experienceYears', e.target.value)} />
              </div>

              {faculty && (
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
              {saving ? <span className="spinner" /> : (faculty ? 'Save changes' : 'Add faculty')}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

function AssignmentsModal({ faculty, onClose }) {
  const [assignments, setAssignments] = useState([]);
  const [loading, setLoading]   = useState(true);
  const [sel, setSel]           = useState({});
  const [academicYear, setYear] = useState('2026-27');
  const [description, setDesc] = useState('');
  const [saving, setSaving]     = useState(false);
  const [subjectNames, setSubjectNames] = useState({});
  const [batchNames, setBatchNames] = useState({});

  const load = () => {
    setLoading(true);
    facultyAPI.getAssignments(faculty.id)
      .then(r => {
        const list = r.data.data || [];
        setAssignments(list);
        list.forEach(a => {
          courseAPI.getSubjectById(a.subjectId).then(res =>
            setSubjectNames(m => ({ ...m, [a.subjectId]: res.data.data.name }))).catch(()=>{});
          courseAPI.getBatchById(a.batchId).then(res =>
            setBatchNames(m => ({ ...m, [a.batchId]: res.data.data.name }))).catch(()=>{});
        });
      })
      .catch(() => toast.error('Failed to load assignments'))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, [faculty.id]);

  const addAssignment = async () => {
    if (!sel.subjectId || !sel.batchId || !sel.semester) {
      return toast.error('Select department, course, semester, subject and batch');
    }
    setSaving(true);
    try {
      await facultyAPI.assignSubject({
        facultyId: faculty.id,
        subjectId: +sel.subjectId,
        batchId: +sel.batchId,
        semester: +sel.semester,
        academicYear,
        description,
      });
      toast.success('Assignment added');
      setSel({}); setDesc('');
      load();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to add assignment');
    } finally { setSaving(false); }
  };

  const removeAssignment = async id => {
    if (!window.confirm('Remove this teaching assignment?')) return;
    try {
      await facultyAPI.removeAssignment(id);
      toast.success('Assignment removed');
      load();
    } catch (err) { toast.error(err.response?.data?.message || 'Failed'); }
  };

  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
      <div className="modal modal-lg">
        <div className="modal-header">
          <h3>Teaching assignments — {faculty.fullName}</h3>
          <button className="modal-close" onClick={onClose}>✕</button>
        </div>
        <div className="modal-body">
          <p style={{ fontSize:12.5, color:'var(--text-muted)', marginBottom:16 }}>
            A faculty member can hold many assignments — each one pins one
            department/course/semester/subject/batch combination, so a single
            faculty can teach across multiple departments, courses, semesters,
            subjects and batches.
          </p>

          {loading ? (
            <div style={{ textAlign:'center', padding:20 }}><div className="spinner spinner-dark" style={{ margin:'0 auto' }} /></div>
          ) : assignments.length === 0 ? (
            <div className="empty-state" style={{ padding:20, marginBottom:20 }}>
              <div className="empty-icon">📚</div>
              <p>No assignments yet</p>
            </div>
          ) : (
            <div className="table-wrap mb-4">
              <table>
                <thead><tr><th>Subject</th><th>Batch</th><th>Sem</th><th>Year</th><th>Description</th><th></th></tr></thead>
                <tbody>
                  {assignments.map(a => (
                    <tr key={a.id}>
                      <td style={{ fontWeight:500 }}>{subjectNames[a.subjectId] || `Subject #${a.subjectId}`}</td>
                      <td>{batchNames[a.batchId] || `Batch #${a.batchId}`}</td>
                      <td>Sem {a.semester}</td>
                      <td className="text-sm text-muted">{a.academicYear}</td>
                      <td className="text-sm">{a.description || '—'}</td>
                      <td>
                        <button className="btn btn-ghost btn-sm" style={{ color:'var(--danger)' }}
                          onClick={() => removeAssignment(a.id)}>Remove</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          <div className="card" style={{ background:'var(--bg)' }}>
            <p style={{ fontSize:13, fontWeight:600, marginBottom:10 }}>Add new assignment</p>
            <AcademicCascadeSelect value={sel} onChange={setSel} />
            <div className="form-grid form-grid-2" style={{ gap:12, marginTop:12 }}>
              <div className="form-group">
                <label className="form-label">Academic year</label>
                <input className="form-input" value={academicYear} onChange={e => setYear(e.target.value)} placeholder="2026-27" />
              </div>
              <div className="form-group">
                <label className="form-label">Description (optional)</label>
                <input className="form-input" value={description} onChange={e => setDesc(e.target.value)}
                  placeholder="e.g. Covers units 1-5, includes lab sessions" />
              </div>
            </div>
            <button className="btn btn-primary" onClick={addAssignment} disabled={saving} style={{ marginTop:8 }}>
              {saving ? <span className="spinner" /> : '+ Add assignment'}
            </button>
          </div>
        </div>
        <div className="modal-footer">
          <button className="btn btn-secondary" onClick={onClose}>Done</button>
        </div>
      </div>
    </div>
  );
}

export default function FacultyPage() {
  const [faculty, setFaculty] = useState([]);
  const [depts, setDepts]     = useState([]);
  const [total, setTotal]     = useState(0);
  const [page, setPage]       = useState(0);
  const [query, setQuery]     = useState('');
  const [modal, setModal]     = useState(null);
  const [assignModal, setAssignModal] = useState(null);
  const [loading, setLoading] = useState(true);
  const [newCred, setNewCred] = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = query
        ? await facultyAPI.search(query, page)
        : await facultyAPI.getAll(page);
      const d = res.data;
      const list = d.data?.content ?? d.data ?? [];
      setFaculty(list);
      setTotal(d.total ?? d.data?.totalElements ?? list.length);
    } catch { toast.error('Failed to load faculty'); }
    setLoading(false);
  }, [page, query]);

  useEffect(() => {
    courseAPI.getDepartments().then(r => setDepts(r.data.data || [])).catch(()=>{});
  }, []);
  useEffect(() => { load(); }, [load]);

  const handleSaved = created => {
    setModal(null);
    if (created) setNewCred(created);
    load();
  };

  const remove = async id => {
    if (!window.confirm(
      'This will PERMANENTLY delete this faculty member and all their teaching ' +
      'assignments and login account. This cannot be undone. Continue?'
    )) return;
    try {
      await facultyAPI.delete(id);
      toast.success('Faculty and their assignments permanently deleted');
      load();
    } catch (err) { toast.error(err.response?.data?.message || 'Failed'); }
  };

  return (
    <div>
      <div className="page-header">
        <div className="page-header-left">
          <h2>Faculty</h2>
          <p>{total} total faculty members</p>
        </div>
        <div className="page-actions">
          <button className="btn btn-primary" onClick={() => setModal('add')}>+ Add faculty</button>
        </div>
      </div>

      <CredentialBanner data={newCred} onClose={() => setNewCred(null)} />

      <div style={{ marginBottom:16 }}>
        <div className="search-bar" style={{ maxWidth:380 }}>
          <span className="search-icon">🔍</span>
          <input placeholder="Search name, email, employee ID…"
            value={query} onChange={e => { setQuery(e.target.value); setPage(0); }} />
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
        ) : faculty.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">🧑‍🏫</div>
            <p>{query ? 'No faculty match your search' : 'No faculty yet. Add one to get started.'}</p>
          </div>
        ) : (
          <table>
            <thead><tr>
              <th>Employee ID</th><th>Name</th><th>Email</th>
              <th>Login</th><th>Department</th><th>Designation</th><th>Status</th><th></th>
            </tr></thead>
            <tbody>
              {faculty.map(f => (
                <tr key={f.id}>
                  <td><span className="badge badge-gray">{f.employeeId}</span></td>
                  <td style={{ fontWeight:500 }}>{f.fullName || `${f.firstName} ${f.lastName}`}</td>
                  <td className="text-muted text-sm">{f.email}</td>
                  <td>
                    {f.loginUsername
                      ? <span className="badge badge-blue">{f.loginUsername}</span>
                      : <span className="text-muted text-sm">—</span>}
                  </td>
                  <td className="text-sm">{depts.find(d => d.id === f.departmentId)?.code || f.departmentId}</td>
                  <td className="text-sm text-muted">{f.designation || '—'}</td>
                  <td>
                    <span className={`badge ${f.status==='ACTIVE'?'badge-green':'badge-gray'}`}>{f.status}</span>
                  </td>
                  <td>
                    <div style={{ display:'flex', gap:6 }}>
                      <button className="btn btn-ghost btn-sm" onClick={() => setAssignModal(f)}>Assignments</button>
                      <button className="btn btn-ghost btn-sm" onClick={() => setModal(f)}>Edit</button>
                      <button className="btn btn-ghost btn-sm" style={{ color:'var(--danger)' }}
                        onClick={() => remove(f.id)}>Remove</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginTop:14 }}>
        <span className="text-sm text-muted">Showing {faculty.length} of {total}</span>
        <div style={{ display:'flex', gap:6 }}>
          <button className="btn btn-secondary btn-sm" disabled={page===0}
            onClick={() => setPage(p=>p-1)}>← Prev</button>
          <button className="btn btn-secondary btn-sm" disabled={faculty.length<10}
            onClick={() => setPage(p=>p+1)}>Next →</button>
        </div>
      </div>

      {modal && (
        <FacultyModal
          faculty={modal === 'add' ? null : modal}
          depts={depts}
          onClose={() => setModal(null)}
          onSaved={handleSaved}
        />
      )}

      {assignModal && (
        <AssignmentsModal faculty={assignModal} onClose={() => setAssignModal(null)} />
      )}
    </div>
  );
}
