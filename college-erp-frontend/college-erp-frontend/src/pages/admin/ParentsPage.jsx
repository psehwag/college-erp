import React, { useEffect, useState, useCallback } from 'react';
import { parentAPI, studentAPI } from '../../services/api';
import toast from 'react-hot-toast';

function CredentialBanner({ data, onClose }) {
  if (!data) return null;
  return (
    <div style={{ padding:'16px 20px', background:'#D1FAE5', border:'1px solid #6EE7B7',
      borderRadius:10, marginBottom:20, display:'flex', justifyContent:'space-between',
      alignItems:'flex-start', gap:12 }}>
      <div>
        <p style={{ fontWeight:600, fontSize:14, color:'#065f46', marginBottom:4 }}>
          ✅ Parent created — login credentials generated
        </p>
        <p style={{ fontSize:13, color:'#065f46' }}>
          <strong>Email:</strong> {data.email} &nbsp;·&nbsp;
          <strong>Default password:</strong> Password@123 &nbsp;·&nbsp;
          <strong>Must change on first login</strong>
        </p>
        <p style={{ fontSize:12, color:'#065f46', marginTop:6 }}>
          ⚠ To link a student to this parent, edit the student and set their Parent ID to <strong>{data.id}</strong>.
        </p>
      </div>
      <button onClick={onClose} style={{ background:'none', border:'none', cursor:'pointer',
        fontSize:18, color:'#065f46' }}>✕</button>
    </div>
  );
}

// ── Link student modal ────────────────────────────────────────────────────
function LinkStudentModal({ parent, onClose, onLinked }) {
  const [students, setStudents] = useState([]);
  const [query, setQuery]       = useState('');
  const [results, setResults]   = useState([]);
  const [linking, setLinking]   = useState(null);

  // Students already linked to this parent
  useEffect(() => {
    studentAPI.getByParent(parent.id)
      .then(r => setStudents(r.data.data || []))
      .catch(() => {});
  }, [parent.id]);

  const search = async () => {
    if (!query.trim()) return;
    try {
      const res = await studentAPI.search(query);
      const list = res.data.data?.content ?? res.data.data ?? [];
      setResults(list);
    } catch { toast.error('Search failed'); }
  };

  const link = async student => {
    setLinking(student.id);
    try {
      await studentAPI.update(student.id, { parentId: parent.id });
      toast.success(`${student.fullName || student.firstName} linked to ${parent.firstName}`);
      setStudents(s => [...s, student]);
      setResults(r => r.filter(x => x.id !== student.id));
      onLinked();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to link student');
    }
    setLinking(null);
  };

  const unlink = async student => {
    setLinking(student.id);
    try {
      await studentAPI.update(student.id, { parentId: null });
      toast.success('Student unlinked');
      setStudents(s => s.filter(x => x.id !== student.id));
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to unlink');
    }
    setLinking(null);
  };

  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
      <div className="modal modal-lg">
        <div className="modal-header">
          <h3>Link students — {parent.firstName} {parent.lastName}</h3>
          <button className="modal-close" onClick={onClose}>✕</button>
        </div>
        <div className="modal-body">

          {/* Already linked */}
          {students.length > 0 && (
            <div style={{ marginBottom:20 }}>
              <p style={{ fontSize:13, fontWeight:600, marginBottom:10 }}>Currently linked students</p>
              <div style={{ display:'flex', flexDirection:'column', gap:8 }}>
                {students.map(s => (
                  <div key={s.id} style={{ display:'flex', alignItems:'center', justifyContent:'space-between',
                    padding:'8px 12px', background:'#F0FDF4', borderRadius:8, border:'1px solid #86EFAC' }}>
                    <div>
                      <span style={{ fontWeight:500, fontSize:13 }}>{s.fullName || s.enrollmentNumber}</span>
                      <span style={{ fontSize:12, color:'var(--text-muted)', marginLeft:8 }}>{s.enrollmentNumber}</span>
                    </div>
                    <button className="btn btn-ghost btn-sm" style={{ color:'var(--danger)' }}
                      disabled={linking === s.id}
                      onClick={() => unlink(s)}>
                      {linking === s.id ? <span className="spinner" /> : 'Unlink'}
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Search */}
          <p style={{ fontSize:13, fontWeight:600, marginBottom:8 }}>Search and link a student</p>
          <div style={{ display:'flex', gap:8, marginBottom:12 }}>
            <input className="form-input" style={{ flex:1 }}
              placeholder="Search by name, enrollment number or email…"
              value={query}
              onChange={e => setQuery(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && search()} />
            <button className="btn btn-primary" onClick={search}>Search</button>
          </div>

          {results.length > 0 && (
            <div style={{ display:'flex', flexDirection:'column', gap:8 }}>
              {results.map(s => (
                <div key={s.id} style={{ display:'flex', alignItems:'center', justifyContent:'space-between',
                  padding:'8px 12px', background:'var(--bg-secondary)', borderRadius:8 }}>
                  <div>
                    <span style={{ fontWeight:500, fontSize:13 }}>{s.fullName || s.enrollmentNumber}</span>
                    <span style={{ fontSize:12, color:'var(--text-muted)', marginLeft:8 }}>{s.email}</span>
                    {s.parentId && (
                      <span className="badge badge-amber" style={{ marginLeft:8, fontSize:10 }}>
                        Already has parent #{s.parentId}
                      </span>
                    )}
                  </div>
                  <button className="btn btn-primary btn-sm"
                    disabled={linking === s.id}
                    onClick={() => link(s)}>
                    {linking === s.id ? <span className="spinner" /> : 'Link'}
                  </button>
                </div>
              ))}
            </div>
          )}

          {results.length === 0 && query && (
            <p style={{ color:'var(--text-muted)', fontSize:13 }}>No students found. Try a different search.</p>
          )}
        </div>
        <div className="modal-footer">
          <button className="btn btn-secondary" onClick={onClose}>Done</button>
        </div>
      </div>
    </div>
  );
}

// ── Parent modal ──────────────────────────────────────────────────────────
function ParentModal({ parent, onClose, onSaved }) {
  const [form, setForm] = useState(parent ? {
    firstName: parent.firstName, lastName: parent.lastName,
    email: parent.email, phone: parent.phone || '',
    alternatePhone: parent.alternatePhone || '', address: parent.address || '',
    occupation: parent.occupation || '', relationToStudent: parent.relationToStudent || 'FATHER',
  } : {
    firstName:'', lastName:'', email:'', phone:'',
    alternatePhone:'', address:'', occupation:'', relationToStudent:'FATHER'
  });
  const [saving, setSaving] = useState(false);
  const set = (k, v) => setForm(f => ({ ...f, [k]: v }));

  const submit = async e => {
    e.preventDefault();
    setSaving(true);
    try {
      if (parent?.id) {
        await parentAPI.update(parent.id, form);
        toast.success('Parent updated');
        onSaved(null);
      } else {
        const res = await parentAPI.create(form);
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
          <h3>{parent ? 'Edit parent' : 'Add parent'}</h3>
          <button className="modal-close" onClick={onClose}>✕</button>
        </div>
        <form onSubmit={submit}>
          <div className="modal-body">
            {!parent && (
              <div style={{ padding:'10px 14px', background:'var(--accent-soft)',
                borderRadius:8, marginBottom:16, fontSize:13, color:'var(--accent)' }}>
                ℹ Login credentials are auto-generated from the email address.
              </div>
            )}
            <div className="form-grid form-grid-2" style={{ gap:14 }}>
              {[['firstName','First name',true],['lastName','Last name',true],
                ['email','Email',!parent],['phone','Phone',false],
                ['alternatePhone','Alternate phone',false],['occupation','Occupation',false]].map(([k,l,req]) => (
                <div className="form-group" key={k}>
                  <label className="form-label">{l}</label>
                  <input className="form-input" value={form[k]||''}
                    onChange={e => set(k, e.target.value)}
                    required={req} type={k==='email'?'email':'text'}
                    disabled={k==='email' && !!parent} />
                </div>
              ))}
              <div className="form-group">
                <label className="form-label">Relation to student</label>
                <select className="form-select" value={form.relationToStudent||'FATHER'}
                  onChange={e => set('relationToStudent', e.target.value)}>
                  {['FATHER','MOTHER','GUARDIAN','SIBLING','OTHER'].map(r => (
                    <option key={r} value={r}>{r}</option>
                  ))}
                </select>
              </div>
              <div className="form-group" style={{ gridColumn:'1/-1' }}>
                <label className="form-label">Address</label>
                <input className="form-input" value={form.address||''}
                  onChange={e => set('address', e.target.value)} />
              </div>
            </div>
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? <span className="spinner" /> : (parent ? 'Save changes' : 'Add parent')}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

// ── Main page ─────────────────────────────────────────────────────────────
export default function ParentsPage() {
  const [parents, setParents]     = useState([]);
  const [total, setTotal]         = useState(0);
  const [page, setPage]           = useState(0);
  const [modal, setModal]         = useState(null);   // null | 'add' | parent object
  const [linkModal, setLinkModal] = useState(null);   // parent object to link students
  const [loading, setLoading]     = useState(true);
  const [newCred, setNewCred]     = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      // Fetch all parents — backend returns a page
      const res = await parentAPI.getAll(page);
      const d = res.data;
      const list = d.data?.content ?? d.data ?? [];
      setParents(Array.isArray(list) ? list : []);
      setTotal(d.total ?? d.data?.totalElements ?? list.length);
    } catch (err) {
      // If backend doesn't have a list-all endpoint yet, show empty gracefully
      console.warn('Could not load parents:', err.response?.status);
      setParents([]);
      setTotal(0);
    }
    setLoading(false);
  }, [page]);

  useEffect(() => { load(); }, [load]);

  const handleSaved = created => {
    setModal(null);
    if (created) {
      setNewCred(created);
      // Open the link-student modal right away so admin can link a student immediately
      setLinkModal(created);
    }
    load();
  };

  const deactivate = async id => {
    if (!window.confirm('Deactivate this parent?')) return;
    try {
      await parentAPI.delete(id);
      toast.success('Parent deactivated');
      load();
    } catch (err) { toast.error(err.response?.data?.message || 'Failed'); }
  };

  return (
    <div>
      <div className="page-header">
        <div className="page-header-left">
          <h2>Parents</h2>
          <p>{total} parent / guardian accounts</p>
        </div>
        <div className="page-actions">
          <button className="btn btn-primary" onClick={() => setModal('add')}>+ Add parent</button>
        </div>
      </div>

      <CredentialBanner data={newCred} onClose={() => setNewCred(null)} />

      <div className="table-wrap">
        {loading ? (
          <div style={{ textAlign:'center', padding:40 }}>
            <div className="spinner spinner-dark" style={{ margin:'0 auto' }} />
          </div>
        ) : parents.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">👨‍👩‍👧</div>
            <p>No parents added yet. Click "Add parent" to create one.</p>
          </div>
        ) : (
          <table>
            <thead><tr>
              <th>Name</th><th>Email</th><th>Phone</th><th>Relation</th><th>Status</th><th>ID</th><th></th>
            </tr></thead>
            <tbody>
              {parents.map(p => (
                <tr key={p.id}>
                  <td style={{ fontWeight:500 }}>{p.firstName} {p.lastName}</td>
                  <td className="text-sm text-muted">{p.email}</td>
                  <td className="text-sm">{p.phone || '—'}</td>
                  <td className="text-sm">{p.relationToStudent || '—'}</td>
                  <td>
                    <span className={`badge ${p.isActive?'badge-green':'badge-gray'}`}>
                      {p.isActive ? 'ACTIVE' : 'INACTIVE'}
                    </span>
                  </td>
                  <td>
                    <span className="badge badge-gray">#{p.id}</span>
                  </td>
                  <td>
                    <div style={{ display:'flex', gap:6 }}>
                      <button className="btn btn-ghost btn-sm"
                        onClick={() => setLinkModal(p)}>Link students</button>
                      <button className="btn btn-ghost btn-sm"
                        onClick={() => setModal(p)}>Edit</button>
                      <button className="btn btn-ghost btn-sm" style={{ color:'var(--danger)' }}
                        onClick={() => deactivate(p.id)}>Remove</button>
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
        <span className="text-sm text-muted">Showing {parents.length} of {total}</span>
        <div style={{ display:'flex', gap:6 }}>
          <button className="btn btn-secondary btn-sm" disabled={page===0}
            onClick={() => setPage(p=>p-1)}>← Prev</button>
          <button className="btn btn-secondary btn-sm" disabled={parents.length<10}
            onClick={() => setPage(p=>p+1)}>Next →</button>
        </div>
      </div>

      {modal && (
        <ParentModal
          parent={modal === 'add' ? null : modal}
          onClose={() => setModal(null)}
          onSaved={handleSaved}
        />
      )}

      {linkModal && (
        <LinkStudentModal
          parent={linkModal}
          onClose={() => setLinkModal(null)}
          onLinked={() => {}}
        />
      )}
    </div>
  );
}
