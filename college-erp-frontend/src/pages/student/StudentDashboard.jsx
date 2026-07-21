import React, { useEffect, useState } from 'react';
import { marksAPI, attendanceAPI, studentAPI, courseAPI } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import useSubjectMap from '../../hooks/useSubjectMap';

const GRADE_COLOR = { O:'badge-blue', A_PLUS:'badge-green', A:'badge-green', B_PLUS:'badge-green',
                      B:'badge-amber', C:'badge-amber', D:'badge-amber', F:'badge-red' };

export default function StudentDashboard() {
  const { user }   = useAuth();
  const sid        = user?.referenceId;
  const { subjects: subjectCache, ensureLoaded, getName } = useSubjectMap();

  const [profile, setProfile]   = useState(null);
  const [course, setCourse]     = useState(null);
  const [marks, setMarks]       = useState([]);
  const [summary, setSummary]   = useState(null);
  const [attStats, setAttStats] = useState([]);
  const [loading, setLoading]   = useState(true);

  useEffect(() => {
    if (!sid) { setLoading(false); return; }

    studentAPI.getById(sid).then(async (profRes) => {
      const p = profRes.data.data;
      setProfile(p);

      if (p.courseId) {
        courseAPI.getCourseById(p.courseId).then(r => setCourse(r.data.data)).catch(()=>{});
      }

      const [marksRes, summaryRes] = await Promise.allSettled([
        marksAPI.getByStudent(sid),
        p.currentSemester ? marksAPI.getSemSummary(sid, p.currentSemester) : Promise.resolve(null),
      ]);

      const marksList = marksRes.status === 'fulfilled' ? (marksRes.value.data.data || []) : [];
      setMarks(marksList);
      if (summaryRes.status === 'fulfilled' && summaryRes.value) setSummary(summaryRes.value.data.data);

      ensureLoaded(marksList.map(m => m.subjectId));

      // Real attendance overview: subjects for the student's current semester's course
      if (p.courseId && p.currentSemester) {
        try {
          const subRes = await courseAPI.getSubjectsByCourse(p.courseId, p.currentSemester);
          const subjectList = subRes.data.data || [];
          const stats = await Promise.all(subjectList.map(async sub => {
            try {
              const pctRes = await attendanceAPI.getPercentage(sid, sub.id);
              return { subjectId: sub.id, name: sub.name, ...pctRes.data.data };
            } catch { return null; }
          }));
          setAttStats(stats.filter(Boolean));
        } catch { setAttStats([]); }
      }
    }).catch(() => {}).finally(() => setLoading(false));
  }, [sid]);

  if (loading) return (
    <div style={{ display:'flex', alignItems:'center', justifyContent:'center', height:300 }}>
      <div className="spinner spinner-dark" style={{ width:32, height:32 }} />
    </div>
  );

  const displayName = user?.name || user?.username || 'Student';
  const overallPct = summary?.overallPercentage ?? null;

  // Sort marks by most recent (fallback to id if no createdAt)
  const recentMarks = [...marks].sort((a, b) => {
    if (a.createdAt && b.createdAt) return new Date(b.createdAt) - new Date(a.createdAt);
    return (b.id || 0) - (a.id || 0);
  }).slice(0, 6);

  return (
    <div>
      {/* Hero */}
      <div className="card mb-6" style={{ background:'linear-gradient(135deg, var(--primary) 0%, var(--primary-light) 100%)', border:'none' }}>
        <div style={{ display:'flex', alignItems:'center', justifyContent:'space-between' }}>
          <div>
            <p style={{ color:'rgba(255,255,255,0.6)', fontSize:13, marginBottom:4 }}>Welcome back</p>
            <h2 style={{ color:'#fff', fontFamily:'var(--font-display)', fontSize:24, fontWeight:700 }}>
              {displayName}
            </h2>
            <p style={{ color:'rgba(255,255,255,0.6)', fontSize:13, marginTop:4 }}>
              {profile?.currentSemester ? `Semester ${profile.currentSemester}` : ''}
              {course?.name ? ` · ${course.name}` : ''}
            </p>
          </div>
          <div style={{ textAlign:'center' }}>
            <div style={{ fontFamily:'var(--font-display)', fontSize:40, fontWeight:700, color:'#fff' }}>
              {overallPct != null ? `${Math.round(overallPct)}%` : '—'}
            </div>
            <div style={{ color:'rgba(255,255,255,0.6)', fontSize:12 }}>Overall score</div>
          </div>
        </div>
      </div>

      <div className="grid-2">
        {/* Recent marks — real subject names, no NaN */}
        <div className="card">
          <h3 style={{ fontFamily:'var(--font-display)', fontSize:15, fontWeight:600, marginBottom:16 }}>
            Recent marks
          </h3>
          {recentMarks.length === 0 ? (
            <div className="empty-state"><div className="empty-icon">📊</div><p>No marks uploaded yet</p></div>
          ) : (
            <div style={{ display:'flex', flexDirection:'column', gap:10 }}>
              {recentMarks.map(m => (
                <div key={m.id} style={{ display:'flex', alignItems:'center', justifyContent:'space-between',
                  padding:'10px 14px', background:'var(--bg)', borderRadius:8 }}>
                  <div>
                    <div style={{ fontSize:13, fontWeight:500 }}>{getName(m.subjectId)}</div>
                    <div style={{ fontSize:11, color:'var(--text-muted)' }}>Semester {m.semester}</div>
                  </div>
                  <div style={{ textAlign:'right' }}>
                    <div style={{ fontFamily:'var(--font-display)', fontWeight:700, fontSize:16 }}>
                      {m.marksObtained}/{m.maxMarks}
                    </div>
                    <span className={`badge ${GRADE_COLOR[m.grade]||'badge-gray'}`} style={{ fontSize:10.5 }}>
                      {m.grade || '—'}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Attendance overview — real data, real subject names */}
        <div className="card">
          <h3 style={{ fontFamily:'var(--font-display)', fontSize:15, fontWeight:600, marginBottom:16 }}>
            Attendance overview
          </h3>
          {attStats.length === 0 ? (
            <div className="empty-state"><div className="empty-icon">✅</div><p>No attendance recorded yet this semester</p></div>
          ) : (
            <div style={{ display:'flex', flexDirection:'column', gap:12 }}>
              {attStats.map(s => {
                const pct = s.percentage ?? 0;
                const ok = pct >= 75;
                return (
                  <div key={s.subjectId}>
                    <div style={{ display:'flex', justifyContent:'space-between', marginBottom:4 }}>
                      <span style={{ fontSize:13, fontWeight:500 }}>{s.name}</span>
                      <span style={{ fontSize:13, fontWeight:600, color: ok ? 'var(--success)' : 'var(--danger)' }}>
                        {pct.toFixed(0)}%
                      </span>
                    </div>
                    <div className="progress-bar">
                      <div className={`progress-fill ${ok?'green':'red'}`} style={{ width:`${Math.min(pct,100)}%` }} />
                    </div>
                    {!ok && <p style={{ fontSize:11, color:'var(--danger)', marginTop:3 }}>⚠ Below 75% — shortfall</p>}
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
