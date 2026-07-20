import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { authAPI } from '../services/api';
import toast from 'react-hot-toast';

const roleHome = { ADMIN: '/admin', FACULTY: '/faculty', STUDENT: '/student', PARENT: '/parent' };

export default function ChangePasswordPage() {
  const { user, clearMustChange } = useAuth();
  const nav = useNavigate();
  const isForced = user?.mustChangePassword;

  const [form, setForm] = useState({ currentPassword: '', newPassword: '', confirm: '' });
  const [saving, setSaving] = useState(false);
  const [errors, setErrors] = useState({});

  const validate = () => {
    const e = {};
    if (!form.currentPassword) e.currentPassword = 'Required';
    if (!form.newPassword)     e.newPassword = 'Required';
    if (form.newPassword.length < 8) e.newPassword = 'Minimum 8 characters';
    if (form.newPassword !== form.confirm) e.confirm = 'Passwords do not match';
    if (form.newPassword === form.currentPassword)
      e.newPassword = 'New password must differ from current';
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const submit = async e => {
    e.preventDefault();
    if (!validate()) return;
    setSaving(true);
    try {
      await authAPI.changePassword({
        currentPassword: form.currentPassword,
        newPassword: form.newPassword,
      });
      toast.success('Password changed successfully!');
      clearMustChange();
      nav(roleHome[user?.role] || '/');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to change password');
    } finally { setSaving(false); }
  };

  const set = (k, v) => setForm(f => ({ ...f, [k]: v }));

  return (
    <div className="auth-page">
      <div className="auth-left">
        <div className="auth-brand">
          <div style={{ fontSize:48, marginBottom:16 }}>🔐</div>
          <h1>{isForced ? 'Set your\nnew password' : 'Change\npassword'}</h1>
          <p style={{ marginTop:16 }}>
            {isForced
              ? 'Your account was created by an administrator. Please set a personal password before continuing.'
              : 'Choose a strong password to keep your account secure.'}
          </p>
          <div style={{ marginTop:32, padding:'16px 20px', background:'rgba(255,255,255,0.08)',
            borderRadius:10 }}>
            <p style={{ color:'rgba(255,255,255,0.7)', fontSize:13 }}>Password must be:</p>
            <ul style={{ color:'rgba(255,255,255,0.5)', fontSize:12.5, marginTop:8, paddingLeft:16 }}>
              <li>At least 8 characters</li>
              <li>Different from your current password</li>
            </ul>
          </div>
        </div>
      </div>

      <div className="auth-right">
        <div className="auth-form-wrap">
          <h2>{isForced ? 'Set new password' : 'Change password'}</h2>
          {isForced && (
            <div style={{ padding:'10px 14px', background:'#FEF3C7', borderRadius:8,
              marginBottom:20, fontSize:13, color:'#92400e' }}>
              ⚠ You must change your default password before accessing the system.
            </div>
          )}
          <p className="auth-sub">
            Signed in as <strong>{user?.username}</strong> ({user?.role?.toLowerCase()})
          </p>

          <form onSubmit={submit} style={{ display:'flex', flexDirection:'column', gap:16 }}>
            <div className="form-group">
              <label className="form-label">Current password</label>
              <input className="form-input" type="password"
                placeholder={isForced ? 'Password@123' : 'Current password'}
                value={form.currentPassword}
                onChange={e => set('currentPassword', e.target.value)} />
              {errors.currentPassword && (
                <span style={{ fontSize:12, color:'var(--danger)' }}>{errors.currentPassword}</span>
              )}
            </div>

            <div className="form-group">
              <label className="form-label">New password</label>
              <input className="form-input" type="password" placeholder="Minimum 8 characters"
                value={form.newPassword}
                onChange={e => set('newPassword', e.target.value)} />
              {errors.newPassword && (
                <span style={{ fontSize:12, color:'var(--danger)' }}>{errors.newPassword}</span>
              )}
            </div>

            <div className="form-group">
              <label className="form-label">Confirm new password</label>
              <input className="form-input" type="password" placeholder="Repeat new password"
                value={form.confirm}
                onChange={e => set('confirm', e.target.value)} />
              {errors.confirm && (
                <span style={{ fontSize:12, color:'var(--danger)' }}>{errors.confirm}</span>
              )}
            </div>

            <button className="btn btn-primary"
              style={{ width:'100%', justifyContent:'center', padding:'11px', marginTop:4 }}
              disabled={saving}>
              {saving ? <span className="spinner" /> : 'Change password'}
            </button>

            {!isForced && (
              <button type="button" className="btn btn-secondary"
                style={{ width:'100%', justifyContent:'center' }}
                onClick={() => nav(-1)}>
                Cancel
              </button>
            )}
          </form>
        </div>
      </div>
    </div>
  );
}
