import React, { useEffect, useState } from 'react';
import { studentAPI, attendanceAPI, marksAPI } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import useSubjectMap from '../../hooks/useSubjectMap';
import toast from 'react-hot-toast';

const GRADE_COLOR = { O:'badge-blue', A_PLUS:'badge-green', A:'badge-green',
  B_PLUS:'badge-amber', B:'badge-amber', C:'badge-amber', D:'badge-gray', F:'badge-red' };

export default function ParentDashboard() {
  const { user } = useAuth();
  const parentId = user?.referenceId; // this is the PARENT record id, NOT a student id
  const { getName, ensureLoaded } = useSubjectMap();

  const [children, setChildren]   = useState([]);
  const [selected, setSelected]   = useState(null);
  const [attStats, setAttStats]   = useState([]);
  const [marks, setMarks]         = useState([]);
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
      .catch(err => {
        if (err.response?.status !== 403) toast.error('Failed to load children data');
      })
      .finally(() => setLoading(false));
  }, [parentId]);

  useEffect(() => {
    if (!selected) return;
    setLoadingData(true);
    Promise.all([
      attendanceAPI.getByStudent(selected.id),
      marksAPI.getByStudent(selected.id),
    ]).then(([attRes, marksRes]) => {
      const recs = attRes.data.data || [];
      const marksList = marksRes.data.data || [];

      const subjectIds = [...new Set([...recs.map(r => r.subjectId), ...marksList.map(m => m.subjectId)])];
      ensureLoaded(subjectIds);

      Promise.all(subjectIds
        .filter(sid => recs.some(r => r.subjectId === sid))
        .map(sid => attendanceAPI.getPercentage(selected.id, sid).then(r => r.data.data).catch(()=>null))
      ).then(pcts => setAttStats(pcts.filter(Boolean)));

      setMarks(marksList);
    }).catch(()=>{}).finally(()=>setLoadingData(false));
  }, [selected]);

  if (loading) return (
    <div style={{ textAlign:'center', padding:60 }}>
      <div className="spinner spinner-dark" style={{ margin:'0 auto' }} />
    </div>
  );

  if (children.length === 0) return (
    <div>
      <div className="page-header">
        <div className="page-header-left">
          <h2>Parent Portal</h2>
        </div>
      </div>
      <div className="empty-state card" style={{ marginTop:20 }}>
        <div className="empty-icon">👨‍👩‍👧</div>
        <p style={{ fontWeight:600, marginBottom:8 }}>No students linked to your account yet.</p>
        <p style={{ fontSize:13, color:'var(--text-muted)' }}>
          Ask the administrator to go to <strong>Parents → Link students</strong>
          and link your child's record to your account.
        </p>
      </div>
    </div>
  );

  return (
    <div>
      <div className="page-header">
        <div className="page-header-left">
          <h2>Parent Portal</h2>
          <p>Track your child's academic progress</p>
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

      {selected && (
        <>
          <div className="card mb-4" style={{ display:'flex', gap:20, alignItems:'flex-start' }}>
            <div style={{ width:52, height:52, borderRadius:'50%', background:'var(--accent-soft)',
              display:'flex', alignItems:'center', justifyContent:'center',
              fontWeight:700, fontSize:18, color:'var(--accent)', flexShrink:0 }}>
              {selected.firstName?.[0]}{selected.lastName?.[0]}
            </div>
            <div>
              <h3 style={{ fontFamily:'var(--font-display)', fontWeight:700, fontSize:17 }}>
                {selected.fullName}
              </h3>
              <p style={{ color:'var(--text-muted)', fontSize:13, marginTop:2 }}>
                {selected.enrollmentNumber} · Semester {selected.currentSemester}
              </p>
              <span className={`badge ${selected.status==='ACTIVE'?'badge-green':'badge-gray'}`}
                style={{ marginTop:6 }}>{selected.status}</span>
            </div>
          </div>

          {loadingData ? (
            <div style={{ textAlign:'center', padding:40 }}>
              <div className="spinner spinner-dark" style={{ margin:'0 auto' }} />
            </div>
          ) : (
            <div style={{ display:'grid', gridTemplateColumns:'1fr 1fr', gap:20 }}>
              <div className="card">
                <h3 style={{ fontFamily:'var(--font-display)', fontSize:14, fontWeight:600, marginBottom:16 }}>
                  📅 Attendance
                </h3>
                {attStats.length === 0 ? (
                  <p style={{ color:'var(--text-muted)', fontSize:13 }}>No attendance records yet.</p>
                ) : attStats.map(s => (
                  <div key={s.subjectId} style={{ display:'flex', alignItems:'center',
                    justifyContent:'space-between', marginBottom:12 }}>
                    <span style={{ fontSize:13 }}>{getName(s.subjectId)}</span>
                    <div style={{ display:'flex', alignItems:'center', gap:8 }}>
                      <div style={{ width:80, height:6, background:'#e5e7eb', borderRadius:3 }}>
                        <div style={{ width:`${Math.min(s.percentage,100)}%`, height:'100%',
                          background: s.percentage>=75 ? 'var(--success)' : 'var(--danger)',
                          borderRadius:3 }} />
                      </div>
                      <span style={{ fontSize:12, fontWeight:600,
                        color: s.percentage>=75 ? 'var(--success)' : 'var(--danger)' }}>
                        {s.percentage.toFixed(0)}%
                      </span>
                    </div>
                  </div>
                ))}
              </div>

              <div className="card">
                <h3 style={{ fontFamily:'var(--font-display)', fontSize:14, fontWeight:600, marginBottom:16 }}>
                  📊 Recent marks
                </h3>
                {marks.length === 0 ? (
                  <p style={{ color:'var(--text-muted)', fontSize:13 }}>No marks uploaded yet.</p>
                ) : marks.slice(0,6).map(m => (
                  <div key={m.id} style={{ display:'flex', justifyContent:'space-between',
                    alignItems:'center', marginBottom:10 }}>
                    <div>
                      <div style={{ fontSize:13 }}>{getName(m.subjectId)}</div>
                      <div style={{ fontSize:11, color:'var(--text-muted)' }}>
                        Semester {m.semester}
                      </div>
                    </div>
                    <div style={{ display:'flex', alignItems:'center', gap:8 }}>
                      <span style={{ fontSize:14, fontWeight:600 }}>
                        {m.marksObtained}/{m.maxMarks}
                      </span>
                      <span className={`badge ${GRADE_COLOR[m.grade]||'badge-gray'}`} style={{ fontSize:11 }}>
                        {m.grade || '—'}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
}
