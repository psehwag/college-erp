import React, { useEffect, useState, useCallback } from 'react';
import { parentAPI } from '../../services/api';
import toast from 'react-hot-toast';

function CredentialBanner({ data, onClose }) {
  if (!data) return null;
  const username = data.userId ? (data.email?.split('@')[0] || 'see below') : '—';
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
          <strong>Password:</strong> Password@123 &nbsp;·&nbsp;
          <strong>Must change password on first login</strong>
        </p>
      </div>
      <button onClick={onClose} style={{ background:'none', border:'none', cursor:'pointer',
        fontSize:18, color:'#065f46' }}>✕</button>
    </div>
  );
}

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
                ℹ Login credentials will be auto-generated using the email address.
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

export default function ParentsPage() {
  const [parents, setParents] = useState([]);
  const [total, setTotal]     = useState(0);
  const [page, setPage]       = useState(0);
  const [modal, setModal]     = useState(null);
  const [loading, setLoading] = useState(true);
  const [newCred, setNewCred] = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      // Backend doesn't have a list-all-parents endpoint yet,
      // so we show an empty state until records are created
      setParents([]);
      setTotal(0);
    } catch { toast.error('Failed to load parents'); }
    setLoading(false);
  }, [page]);

  useEffect(() => { load(); }, [load]);

  const handleSaved = created => {
    setModal(null);
    if (created) {
      setNewCred(created);
      setParents(p => [created, ...p]);
      setTotal(t => t + 1);
    } else {
      load();
    }
  };

  const deactivate = async id => {
    if (!window.confirm('Deactivate this parent?')) return;
    try {
      await parentAPI.delete(id);
      toast.success('Parent deactivated');
      setParents(p => p.filter(x => x.id !== id));
    } catch (err) { toast.error(err.response?.data?.message || 'Failed'); }
  };

  return (
    <div>
      <div className="page-header">
        <div className="page-header-left">
          <h2>Parents</h2>
          <p>Manage parent / guardian accounts</p>
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
              <th>Name</th><th>Email</th><th>Phone</th><th>Relation</th><th>Status</th><th></th>
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
                    <div style={{ display:'flex', gap:6 }}>
                      <button className="btn btn-ghost btn-sm" onClick={() => setModal(p)}>Edit</button>
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

      {modal && (
        <ParentModal
          parent={modal === 'add' ? null : modal}
          onClose={() => setModal(null)}
          onSaved={handleSaved}
        />
      )}
    </div>
  );
}
