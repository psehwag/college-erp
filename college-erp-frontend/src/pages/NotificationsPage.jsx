import React, { useEffect, useState } from 'react';
import { notifAPI, adminAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';
import { formatDistanceToNow } from 'date-fns';

const TYPE_ICON = {
  ATTENDANCE_SHORTFALL:'⚠️', MARKS_UPLOADED:'📊', EXAM_SCHEDULE:'📅',
  GENERAL_ANNOUNCEMENT:'📢', DEFAULTER_WARNING:'🚨', TIMETABLE_CHANGE:'🔄',
  RESULT_PUBLISHED:'🎓', FACE_ENROLLMENT_REQUIRED:'🤖', FEE_REMINDER:'💳',
};

const ROLES = ['STUDENT', 'FACULTY', 'PARENT'];

function ComposeAnnouncement({ onSent }) {
  const [open, setOpen]       = useState(false);
  const [title, setTitle]     = useState('');
  const [message, setMessage] = useState('');
  const [roles, setRoles]     = useState([]);
  const [sendEmail, setSendEmail] = useState(false);
  const [sending, setSending] = useState(false);

  const toggleRole = r => setRoles(prev => prev.includes(r) ? prev.filter(x => x !== r) : [...prev, r]);

  const send = async () => {
    if (!title.trim() || !message.trim()) return toast.error('Title and message are required');
    if (roles.length === 0) return toast.error('Select at least one recipient role');
    setSending(true);
    try {
      await adminAPI.broadcast({ title, message, targetRoles: roles, sendEmail });
      toast.success('Announcement sent to active users of the selected role(s)');
      setTitle(''); setMessage(''); setRoles([]); setSendEmail(false); setOpen(false);
      onSent?.();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to send announcement');
    } finally { setSending(false); }
  };

  return (
    <div className="card mb-6">
      <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center', cursor:'pointer' }}
        onClick={() => setOpen(o => !o)}>
        <h3 style={{ fontFamily:'var(--font-display)', fontSize:15, fontWeight:600 }}>
          📢 Compose announcement
        </h3>
        <span style={{ fontSize:13, color:'var(--accent)' }}>{open ? 'Close ▲' : 'New ▼'}</span>
      </div>

      {open && (
        <div style={{ marginTop:16, display:'flex', flexDirection:'column', gap:12 }}>
          <div className="form-group">
            <label className="form-label">Title</label>
            <input className="form-input" value={title} onChange={e => setTitle(e.target.value)}
              placeholder="e.g. Semester exam schedule released" />
          </div>
          <div className="form-group">
            <label className="form-label">Message</label>
            <textarea className="form-input" rows={3} value={message} onChange={e => setMessage(e.target.value)}
              placeholder="Write the announcement..." />
          </div>
          <div className="form-group">
            <label className="form-label">Send to (select one or more)</label>
            <div style={{ display:'flex', gap:16 }}>
              {ROLES.map(r => (
                <label key={r} style={{ display:'flex', alignItems:'center', gap:6, cursor:'pointer', fontSize:13 }}>
                  <input type="checkbox" checked={roles.includes(r)} onChange={() => toggleRole(r)} />
                  {r}
                </label>
              ))}
            </div>
            <p style={{ fontSize:11.5, color:'var(--text-muted)', marginTop:6 }}>
              Only active accounts receive this — deactivated users are automatically skipped.
            </p>
          </div>
          <label style={{ display:'flex', alignItems:'center', gap:6, cursor:'pointer', fontSize:13 }}>
            <input type="checkbox" checked={sendEmail} onChange={e => setSendEmail(e.target.checked)} />
            Also send via email
          </label>
          <button className="btn btn-primary" onClick={send} disabled={sending} style={{ alignSelf:'flex-start' }}>
            {sending ? <span className="spinner" /> : 'Send announcement'}
          </button>
        </div>
      )}
    </div>
  );
}

export default function NotificationsPage() {
  const { user } = useAuth();
  const [notifs, setNotifs]   = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage]       = useState(0);

  const load = async (p=0) => {
    setLoading(true);
    try {
      const r = await notifAPI.getMyNotifs(p);
      // The backend returns { data: [...] } — data is already the array
      // (NOT a paginated {content:[...]} wrapper). Reading .content here
      // was the bug that made this page always look empty.
      const list = r.data?.data;
      setNotifs(Array.isArray(list) ? list : []);
    } catch {
      toast.error('Failed to load notifications');
      setNotifs([]);
    }
    setLoading(false);
  };

  useEffect(() => { load(page); }, [page]);

  const markRead = async id => {
    try {
      await notifAPI.markRead(id);
      setNotifs(n => n.map(x => x.id===id ? {...x, isRead:true} : x));
    } catch {}
  };

  const markAll = async () => {
    try {
      await notifAPI.markAllRead();
      setNotifs(n => n.map(x => ({...x, isRead:true})));
      toast.success('All marked as read');
    } catch {}
  };

  const unreadCount = notifs.filter(n => !n.isRead).length;

  return (
    <div>
      <div className="page-header">
        <div className="page-header-left">
          <h2>Notifications</h2>
          <p>{unreadCount} unread</p>
        </div>
        {unreadCount > 0 && (
          <button className="btn btn-secondary" onClick={markAll}>Mark all read</button>
        )}
      </div>

      {user?.role === 'ADMIN' && <ComposeAnnouncement onSent={() => load(page)} />}

      {loading ? (
        <div style={{ textAlign:'center', padding:60 }}><div className="spinner spinner-dark" style={{ margin:'0 auto' }} /></div>
      ) : notifs.length === 0 ? (
        <div className="empty-state card">
          <div className="empty-icon">🔔</div>
          <p>No notifications yet</p>
        </div>
      ) : (
        <div style={{ display:'flex', flexDirection:'column', gap:8 }}>
          {notifs.map(n => (
            <div key={n.id}
              onClick={() => !n.isRead && markRead(n.id)}
              style={{ background:'var(--surface)', border:`1px solid ${n.isRead ? 'var(--border)' : 'var(--accent)'}`,
                borderRadius:10, padding:'14px 18px', cursor: n.isRead ? 'default' : 'pointer',
                display:'flex', gap:14, alignItems:'flex-start',
                opacity: n.isRead ? 0.7 : 1 }}>
              <div style={{ fontSize:24, flexShrink:0 }}>{TYPE_ICON[n.type] || '📬'}</div>
              <div style={{ flex:1 }}>
                <div style={{ display:'flex', justifyContent:'space-between', marginBottom:3 }}>
                  <span style={{ fontWeight:600, fontSize:14 }}>{n.title}</span>
                  <span style={{ fontSize:11, color:'var(--text-muted)' }}>
                    {n.createdAt ? formatDistanceToNow(new Date(n.createdAt), { addSuffix:true }) : ''}
                  </span>
                </div>
                <p style={{ fontSize:13, color:'var(--text-secondary)', lineHeight:1.5 }}>{n.message}</p>
                {!n.isRead && (
                  <span style={{ fontSize:11, color:'var(--accent)', fontWeight:500, marginTop:4, display:'block' }}>
                    Click to mark as read
                  </span>
                )}
              </div>
              {!n.isRead && (
                <div style={{ width:8, height:8, borderRadius:'50%', background:'var(--accent)', flexShrink:0, marginTop:6 }} />
              )}
            </div>
          ))}
        </div>
      )}

      <div style={{ display:'flex', gap:8, marginTop:16, justifyContent:'center' }}>
        <button className="btn btn-secondary btn-sm" disabled={page===0} onClick={() => setPage(p=>p-1)}>← Prev</button>
        <button className="btn btn-secondary btn-sm" disabled={notifs.length<20} onClick={() => setPage(p=>p+1)}>Next →</button>
      </div>
    </div>
  );
}
