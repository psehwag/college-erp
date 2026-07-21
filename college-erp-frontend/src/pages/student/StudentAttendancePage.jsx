import React, { useEffect, useState } from 'react';
import { attendanceAPI } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import useSubjectMap from '../../hooks/useSubjectMap';
import toast from 'react-hot-toast';

const STATUS_COLOR = { PRESENT:'badge-green', ABSENT:'badge-red', LATE:'badge-amber', EXCUSED:'badge-gray' };

export default function StudentAttendancePage() {
  const { user } = useAuth();
  const studentId = user?.referenceId;
  const { getName, ensureLoaded } = useSubjectMap();

  const [records, setRecords]   = useState([]);
  const [stats, setStats]       = useState([]);
  const [loading, setLoading]   = useState(true);

  useEffect(() => {
    if (!studentId) { setLoading(false); return; }
    setLoading(true);

    attendanceAPI.getByStudent(studentId).then(async (attRes) => {
      const recs = attRes.data.data || [];
      setRecords(recs);

      const subjectIds = [...new Set(recs.map(r => r.subjectId))];
      ensureLoaded(subjectIds);

      const pcts = await Promise.all(
        subjectIds.map(sid =>
          attendanceAPI.getPercentage(studentId, sid)
            .then(r => r.data.data)
            .catch(() => null)
        )
      );
      setStats(pcts.filter(Boolean));
    }).catch(() => {
      toast.error('Failed to load attendance data');
    }).finally(() => setLoading(false));
  }, [studentId]);

  if (loading) return (
    <div style={{ textAlign:'center', padding:60 }}>
      <div className="spinner spinner-dark" style={{ margin:'0 auto' }} />
    </div>
  );

  return (
    <div>
      <div className="page-header">
        <div className="page-header-left">
          <h2>My Attendance</h2>
          <p>Subject-wise attendance summary</p>
        </div>
      </div>

      {stats.length > 0 && (
        <div style={{ display:'grid', gridTemplateColumns:'repeat(auto-fill, minmax(220px, 1fr))', gap:14, marginBottom:24 }}>
          {stats.map(s => (
            <div key={s.subjectId} className="card"
              style={{ borderLeft:`4px solid ${s.percentage>=75?'var(--success)':'var(--danger)'}` }}>
              <div style={{ fontWeight:600, fontSize:13, marginBottom:8 }}>
                {getName(s.subjectId)}
              </div>
              <div style={{ fontFamily:'var(--font-display)', fontSize:28, fontWeight:700,
                color: s.percentage>=75 ? 'var(--success)' : 'var(--danger)' }}>
                {s.percentage.toFixed(1)}%
              </div>
              <div style={{ fontSize:12, color:'var(--text-muted)', marginTop:4 }}>
                {s.presentClasses}/{s.totalClasses} classes attended
              </div>
              {s.isShortfall && (
                <span className="badge badge-red" style={{ marginTop:8, fontSize:11 }}>
                  ⚠ Below 75%
                </span>
              )}
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
            <thead><tr>
              <th>Date</th><th>Subject</th><th>Status</th><th>Marked by</th><th>Time</th>
            </tr></thead>
            <tbody>
              {records.slice().reverse().map(r => (
                <tr key={r.id}>
                  <td>{r.attendanceDate}</td>
                  <td className="text-sm">{getName(r.subjectId)}</td>
                  <td><span className={`badge ${STATUS_COLOR[r.status]||'badge-gray'}`}>{r.status}</span></td>
                  <td className="text-sm text-muted">{r.markedBy?.replace('_', ' ')}</td>
                  <td className="text-sm text-muted">{r.checkInTime || '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
