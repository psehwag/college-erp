import React, { useEffect, useState, useCallback } from 'react';
import { authAPI } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import toast from 'react-hot-toast';

function AdminModal({ admin, onClose, onSaved }) {
  const [form, setForm] = useState(admin
    ? { name: admin.name || '', email: admin.email }
    : { username:'', name:'', email:'', password:'' });
  const [saving, setSaving] = useState(false);
  const set = (k,v) => setForm(f => ({ ...f, [k]: v }));

  const submit = async e => {
    e.preventDefault();
    setSaving(true);
    try {
      if (admin) {
        await authAPI.updateAdmin(admin.id, { name: form.name, email: form.email });
        toast.success('Admin updated');
      } else {
        await authAPI.createAdmin(form);
        toast.success('Admin account created');
      }
      onSaved();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to save');
    } finally { setSaving(false); }
  };

  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
      <div className="modal">
        <div className="modal-header">
          <h3>{admin ? 'Edit admin' : 'Add admin'}</h3>
          <button className="modal-close" onClick={onClose}>✕</button>
        </div>
        <form onSubmit={submit}>
          <div className="modal-body">
            <div style={{ display:'flex', flexDirection:'column', gap:14 }}>
              {!admin && (
                <div className="form-group">
                  <label className="form-label">Username</label>
                  <input className="form-input" value={form.username} required
                    onChange={e => set('username', e.target.value)} />
                </div>
              )}
              <div className="form-group">
                <label className="form-label">Full name</label>
                <input className="form-input" value={form.name}
                  onChange={e => set('name', e.target.value)} />
              </div>
              <div className="form-group">
                <label className="form-label">Email</label>
                <input className="form-input" type="email" value={form.email} required
                  onChange={e => set('email', e.target.value)} />
              </div>
              {!admin && (
                <div className="form-group">
                  <label className="form-label">Password</label>
                  <input className="form-input" type="password" value={form.password} required minLength={8}
                    onChange={e => set('password', e.target.value)} />
                </div>
              )}
            </div>
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? <span className="spinner" /> : (admin ? 'Save changes' : 'Create admin')}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default function AdminsPage() {
  const { user } = useAuth();
  const [admins, setAdmins]   = useState([]);
  const [loading, setLoading] = useState(true);
  const [modal, setModal]     = useState(null);

  const load = useCallback(() => {
    setLoading(true);
    authAPI.listAdmins()
      .then(r => setAdmins(r.data.data || []))
      .catch(() => toast.error('Failed to load admins'))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => { load(); }, [load]);

  const remove = async (a) => {
    if (a.id === user?.id) return toast.error('You cannot delete your own admin account');
    if (!window.confirm(`Permanently delete admin "${a.username}"?`)) return;
    try {
      await authAPI.deleteAdmin(a.id);
      toast.success('Admin deleted');
      load();
    } catch (err) { toast.error(err.response?.data?.message || 'Failed to delete'); }
  };

  return (
    <div>
      <div className="page-header">
        <div className="page-header-left">
          <h2>Admin accounts</h2>
          <p>{admins.length} admin account{admins.length!==1?'s':''}</p>
        </div>
        <div className="page-actions">
          <button className="btn btn-primary" onClick={() => setModal('add')}>+ Add admin</button>
        </div>
      </div>

      <div className="table-wrap">
        {loading ? (
          <div style={{ textAlign:'center', padding:40 }}>
            <div className="spinner spinner-dark" style={{ margin:'0 auto' }} />
          </div>
        ) : admins.length === 0 ? (
          <div className="empty-state"><div className="empty-icon">🛡️</div><p>No admin accounts found</p></div>
        ) : (
          <table>
            <thead><tr><th>Username</th><th>Name</th><th>Email</th><th>Status</th><th></th></tr></thead>
            <tbody>
              {admins.map(a => (
                <tr key={a.id}>
                  <td><span className="badge badge-blue">{a.username}</span>{a.id===user?.id && <span className="badge badge-gray" style={{ marginLeft:6 }}>You</span>}</td>
                  <td style={{ fontWeight:500 }}>{a.name || '—'}</td>
                  <td className="text-sm text-muted">{a.email}</td>
                  <td><span className={`badge ${a.isActive?'badge-green':'badge-gray'}`}>{a.isActive?'Active':'Inactive'}</span></td>
                  <td>
                    <div style={{ display:'flex', gap:6 }}>
                      <button className="btn btn-ghost btn-sm" onClick={() => setModal(a)}>Edit</button>
                      <button className="btn btn-ghost btn-sm" style={{ color:'var(--danger)' }}
                        disabled={a.id===user?.id}
                        onClick={() => remove(a)}>Delete</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {modal && (
        <AdminModal
          admin={modal === 'add' ? null : modal}
          onClose={() => setModal(null)}
          onSaved={() => { setModal(null); load(); }}
        />
      )}
    </div>
  );
}
