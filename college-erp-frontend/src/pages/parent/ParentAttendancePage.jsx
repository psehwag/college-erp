import React, { useEffect, useState } from 'react';
import { studentAPI, attendanceAPI } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import useSubjectMap from '../../hooks/useSubjectMap';
import toast from 'react-hot-toast';

const STATUS_COLOR = { PRESENT:'badge-green', ABSENT:'badge-red', LATE:'badge-amber', EXCUSED:'badge-gray' };

export default function ParentAttendancePage() {
  const { user } = useAuth();
  const parentId = user?.referenceId;
  const { getName, ensureLoaded } = useSubjectMap();

  const [children, setChildren]   = useState([]);
  const [selected, setSelected]   = useState(null);
  const [records, setRecords]     = useState([]);
  const [stats, setStats]         = useState([]);
  const [loading, setLoading]     = useState(true);
  const [loadingData, setLoadingData] = useState(false);

  useEffect(() => {
    if (!parentId) { setLoading(false); return; }
    studentAPI.getByParent(parentId)
      .then(r => {
        const list = r.data.data || [];
        setChildren(list);
        if (list.length >= 1) setSelected(list[0]);
      })
      .catch(() => toast.error('Failed to load children'))
      .finally(() => setLoading(false));
  }, [parentId]);

  useEffect(() => {
    if (!selected) return;
    setLoadingData(true);
    attendanceAPI.getByStudent(selected.id).then(async (attRes) => {
      const recs = attRes.data.data || [];
      setRecords(recs);

      const subjectIds = [...new Set(recs.map(r => r.subjectId))];
      ensureLoaded(subjectIds);

      const pcts = await Promise.all(
        subjectIds.map(sid => attendanceAPI.getPercentage(selected.id, sid).then(r => r.data.data).catch(() => null))
      );
      setStats(pcts.filter(Boolean));
    }).catch(() => toast.error('Failed to load attendance')).finally(() => setLoadingData(false));
  }, [selected]);

  if (loading) return (
    <div style={{ textAlign:'center', padding:60 }}>
      <div className="spinner spinner-dark" style={{ margin:'0 auto' }} />
    </div>
  );

  if (children.length === 0) return (
    <div className="empty-state card" style={{ marginTop:20 }}>
      <div className="empty-icon">👨‍👩‍👧</div>
      <p>No students linked to your account yet.</p>
    </div>
  );

  return (
    <div>
      <div className="page-header">
        <div className="page-header-left">
          <h2>Child's Attendance</h2>
          <p>Subject-wise attendance summary</p>
        </div>
      </div>

      {children.length > 1 && (
        <div style={{ display:'flex', gap:10, marginBottom:20 }}>
          {children.map(c => (
            <button key={c.id}
              className={`btn ${selected?.id===c.id ? 'btn-primary' : 'btn-secondary'}`}
              onClick={() => setSelected(c)}>
              {c.fullName}
            </button>
          ))}
        </div>
      )}

      {loadingData ? (
        <div style={{ textAlign:'center', padding:40 }}>
          <div className="spinner spinner-dark" style={{ margin:'0 auto' }} />
        </div>
      ) : (
        <>
          {stats.length > 0 && (
            <div style={{ display:'grid', gridTemplateColumns:'repeat(auto-fill, minmax(220px, 1fr))', gap:14, marginBottom:24 }}>
              {stats.map(s => (
                <div key={s.subjectId} className="card"
                  style={{ borderLeft:`4px solid ${s.percentage>=75?'var(--success)':'var(--danger)'}` }}>
                  <div style={{ fontWeight:600, fontSize:13, marginBottom:8 }}>{getName(s.subjectId)}</div>
                  <div style={{ fontFamily:'var(--font-display)', fontSize:28, fontWeight:700,
                    color: s.percentage>=75 ? 'var(--success)' : 'var(--danger)' }}>
                    {s.percentage.toFixed(1)}%
                  </div>
                  <div style={{ fontSize:12, color:'var(--text-muted)', marginTop:4 }}>
                    {s.presentClasses}/{s.totalClasses} classes attended
                  </div>
                  {s.isShortfall && <span className="badge badge-red" style={{ marginTop:8, fontSize:11 }}>⚠ Below 75%</span>}
                </div>
              ))}
            </div>
          )}

          {records.length === 0 ? (
            <div className="empty-state card">
              <div className="empty-icon">📋</div>
              <p>No attendance records found yet.</p>
            </div>
          ) : (
            <div className="table-wrap">
              <table>
                <thead><tr><th>Date</th><th>Subject</th><th>Status</th><th>Marked by</th></tr></thead>
                <tbody>
                  {records.slice().reverse().map(r => (
                    <tr key={r.id}>
                      <td>{r.attendanceDate}</td>
                      <td className="text-sm">{getName(r.subjectId)}</td>
                      <td><span className={`badge ${STATUS_COLOR[r.status]||'badge-gray'}`}>{r.status}</span></td>
                      <td className="text-sm text-muted">{r.markedBy?.replace('_',' ')}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </>
      )}
    </div>
  );
}
