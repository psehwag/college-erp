import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';

export default function LoginPage() {
  const { login } = useAuth();
  const nav = useNavigate();
  const [form, setForm] = useState({ usernameOrEmail: '', password: '' });
  const [loading, setLoading] = useState(false);

  const roleHome = { ADMIN: '/admin', FACULTY: '/faculty', STUDENT: '/student', PARENT: '/parent' };

  const submit = async e => {
    e.preventDefault();
    setLoading(true);
    try {
      const user = await login(form);
      // If first-time login (auto-created account), force password change
      if (user.mustChangePassword) {
        toast('Please set a new password before continuing.', { icon: '🔐' });
        nav('/change-password');
        return;
      }
      toast.success(`Welcome back, ${user.name}!`);
      nav(roleHome[user.role] || '/');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Invalid credentials');
    } finally { setLoading(false); }
  };

  return (
    <div className="auth-page">
      <div className="auth-left">
        <div className="auth-brand">
          <div style={{ display:'flex', alignItems:'center', gap:12, marginBottom:32 }}>
            <div style={{ width:44, height:44, background:'rgba(79,110,247,0.25)', borderRadius:10,
              display:'flex', alignItems:'center', justifyContent:'center', fontSize:20 }}>🎓</div>
            <span style={{ fontSize:14, color:'rgba(255,255,255,0.6)', fontWeight:500, letterSpacing:'0.5px' }}>
              SMART ERP LITE
            </span>
          </div>
          <h1>One system.<br />Every role.<br />All in sync.</h1>
          <p style={{ marginTop:16 }}>
            AI-powered face recognition attendance, live dashboards, and complete academic
            record management — built for colleges that move fast.
          </p>
          <div style={{ display:'flex', gap:24, marginTop:48 }}>
            {[['👥','Students'],['🧑‍🏫','Faculty'],['📊','Admin'],['👨‍👩‍👧','Parents']].map(([ic, lb]) => (
              <div key={lb} style={{ textAlign:'center' }}>
                <div style={{ fontSize:24 }}>{ic}</div>
                <div style={{ fontSize:11, color:'rgba(255,255,255,0.45)', marginTop:4 }}>{lb}</div>
              </div>
            ))}
          </div>
        </div>
      </div>

      <div className="auth-right">
        <div className="auth-form-wrap">
          <h2>Sign in</h2>
          <p className="auth-sub">Access your college portal</p>

          <form onSubmit={submit} style={{ display:'flex', flexDirection:'column', gap:16 }}>
            <div className="form-group">
              <label className="form-label">Username or email</label>
              <input className="form-input" placeholder="admin"
                value={form.usernameOrEmail}
                onChange={e => setForm(f => ({ ...f, usernameOrEmail: e.target.value }))}
                required />
            </div>
            <div className="form-group">
              <label className="form-label">Password</label>
              <input className="form-input" type="password" placeholder="••••••••"
                value={form.password}
                onChange={e => setForm(f => ({ ...f, password: e.target.value }))}
                required />
            </div>
            <button className="btn btn-primary"
              style={{ width:'100%', justifyContent:'center', padding:'11px' }}
              disabled={loading}>
              {loading ? <span className="spinner" /> : 'Sign in'}
            </button>
          </form>

          <div style={{ marginTop:28, padding:16, background:'#F8F9FC', borderRadius:8,
            fontSize:12.5, color:'#6B7280' }}>
            <strong style={{ display:'block', marginBottom:6, color:'#374151' }}>Demo credentials</strong>
            admin / Password@123<br />
            faculty1 / Password@123 · student1 / Password@123<br />
            parent1 / Password@123
          </div>
        </div>
      </div>
    </div>
  );
}
