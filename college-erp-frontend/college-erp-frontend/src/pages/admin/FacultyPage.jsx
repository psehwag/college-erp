import React, { useEffect, useState, useCallback } from 'react';
import { facultyAPI, courseAPI } from '../../services/api';
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
  } : {
    firstName:'', lastName:'', email:'', phone:'',
    departmentId:'', designation:'', qualification:'', specialization:'', experienceYears:''
  });
  const [saving, setSaving] = useState(false);
  const set = (k, v) => setForm(f => ({ ...f, [k]: v }));

  const submit = async e => {
    e.preventDefault();
    setSaving(true);
    try {
      if (faculty?.id) {
        await facultyAPI.update(faculty.id, form);
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
                ['email','Email',!faculty],['phone','Phone',false],
                ['designation','Designation',false],['qualification','Qualification',false],
                ['specialization','Specialization',false]].map(([k,l,req]) => (
                <div className="form-group" key={k}>
                  <label className="form-label">{l}</label>
                  <input className="form-input" value={form[k]||''}
                    onChange={e => set(k, e.target.value)}
                    required={req} type={k==='email'?'email':'text'}
                    disabled={k==='email' && !!faculty} />
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
                  onChange={e => set('experienceYears', +e.target.value)} />
              </div>
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

export default function FacultyPage() {
  const [faculty, setFaculty] = useState([]);
  const [depts, setDepts]     = useState([]);
  const [total, setTotal]     = useState(0);
  const [page, setPage]       = useState(0);
  const [query, setQuery]     = useState('');
  const [modal, setModal]     = useState(null);
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

  const deactivate = async id => {
    if (!window.confirm('Deactivate this faculty member?')) return;
    try { await facultyAPI.delete(id); toast.success('Faculty deactivated'); load(); }
    catch (err) { toast.error(err.response?.data?.message || 'Failed'); }
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
                      <button className="btn btn-ghost btn-sm" onClick={() => setModal(f)}>Edit</button>
                      <button className="btn btn-ghost btn-sm" style={{ color:'var(--danger)' }}
                        onClick={() => deactivate(f.id)}>Remove</button>
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
    </div>
  );
}
