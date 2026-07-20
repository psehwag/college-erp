import React, { useEffect, useState } from 'react';
import { notifAPI } from '../../services/api';
import toast from 'react-hot-toast';
import { formatDistanceToNow } from 'date-fns';

const TYPE_ICON = {
  ATTENDANCE_SHORTFALL:'⚠️', MARKS_UPLOADED:'📊', EXAM_SCHEDULE:'📅',
  GENERAL_ANNOUNCEMENT:'📢', DEFAULTER_WARNING:'🚨', TIMETABLE_CHANGE:'🔄',
  RESULT_PUBLISHED:'🎓', FACE_ENROLLMENT_REQUIRED:'🤖',
};

export default function NotificationsPage() {
  const [notifs, setNotifs]   = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage]       = useState(0);

  const load = async (p=0) => {
    setLoading(true);
    try {
      const r = await notifAPI.getMyNotifs(p);
      setNotifs(r.data.data?.content || []);
    } catch { toast.error('Failed to load notifications'); }
    setLoading(false);
  };

  useEffect(() => { load(page); }, [page]);

  const markRead = async id => {
    await notifAPI.markRead(id);
    setNotifs(n => n.map(x => x.id===id ? {...x, isRead:true} : x));
  };

  const markAll = async () => {
    await notifAPI.markAllRead();
    setNotifs(n => n.map(x => ({...x, isRead:true})));
    toast.success('All marked as read');
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
