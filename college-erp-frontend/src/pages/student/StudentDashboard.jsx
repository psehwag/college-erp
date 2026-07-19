import React, { useEffect, useState } from 'react';
import { marksAPI, attendanceAPI, courseAPI } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import { RadialBarChart, RadialBar, ResponsiveContainer } from 'recharts';

const GRADE_COLOR = { O:'badge-blue', A_PLUS:'badge-green', A:'badge-green', B_PLUS:'badge-green',
                      B:'badge-amber', C:'badge-amber', D:'badge-amber', F:'badge-red' };

export default function StudentDashboard() {
  const { user }   = useAuth();
  const sid        = user?.referenceId || 1;
  const [marks, setMarks]       = useState([]);
  const [subjects, setSubjects] = useState([]);
  const [loading, setLoading]   = useState(true);

  useEffect(() => {
    Promise.allSettled([
      marksAPI.getByStudent(sid),
      courseAPI.getDepartments(),
    ]).then(([m]) => {
      if (m.status === 'fulfilled') setMarks(m.value.data.data || []);
      setLoading(false);
    });
  }, [sid]);

  if (loading) return (
    <div style={{ display:'flex', alignItems:'center', justifyContent:'center', height:300 }}>
      <div className="spinner spinner-dark" style={{ width:32, height:32 }} />
    </div>
  );

  const semMarks = marks.filter(m => m.semester === 1); // show current sem
  const totalPct = semMarks.length > 0
    ? semMarks.reduce((a,m) => a + m.percentage, 0) / semMarks.length
    : 0;

  return (
    <div>
      {/* Hero */}
      <div className="card mb-6" style={{ background:'linear-gradient(135deg, var(--primary) 0%, var(--primary-light) 100%)', border:'none' }}>
        <div style={{ display:'flex', alignItems:'center', justifyContent:'space-between' }}>
          <div>
            <p style={{ color:'rgba(255,255,255,0.6)', fontSize:13, marginBottom:4 }}>Welcome back</p>
            <h2 style={{ color:'#fff', fontFamily:'var(--font-display)', fontSize:24, fontWeight:700 }}>
              {user?.username}
            </h2>
            <p style={{ color:'rgba(255,255,255,0.6)', fontSize:13, marginTop:4 }}>
              Semester 1 · B.Tech Computer Science
            </p>
          </div>
          <div style={{ textAlign:'center' }}>
            <div style={{ fontFamily:'var(--font-display)', fontSize:40, fontWeight:700, color:'#fff' }}>
              {Math.round(totalPct)}%
            </div>
            <div style={{ color:'rgba(255,255,255,0.6)', fontSize:12 }}>Overall score</div>
          </div>
        </div>
      </div>

      <div className="grid-2">
        {/* Recent marks */}
        <div className="card">
          <h3 style={{ fontFamily:'var(--font-display)', fontSize:15, fontWeight:600, marginBottom:16 }}>
            Recent marks
          </h3>
          {marks.length === 0 ? (
            <div className="empty-state"><div className="empty-icon">📊</div><p>No marks uploaded yet</p></div>
          ) : (
            <div style={{ display:'flex', flexDirection:'column', gap:10 }}>
              {marks.slice(0,6).map(m => (
                <div key={m.id} style={{ display:'flex', alignItems:'center', justifyContent:'space-between',
                  padding:'10px 14px', background:'var(--bg)', borderRadius:8 }}>
                  <div>
                    <div style={{ fontSize:13, fontWeight:500 }}>Subject #{m.subjectId}</div>
                    <div style={{ fontSize:11, color:'var(--text-muted)' }}>{m.examType?.replace('_',' ')}</div>
                  </div>
                  <div style={{ textAlign:'right' }}>
                    <div style={{ fontFamily:'var(--font-display)', fontWeight:700, fontSize:16 }}>
                      {m.marksObtained}/{m.maxMarks}
                    </div>
                    <span className={`badge ${GRADE_COLOR[m.grade]||'badge-gray'}`} style={{ fontSize:10.5 }}>
                      {m.grade}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Attendance summary placeholder */}
        <div className="card">
          <h3 style={{ fontFamily:'var(--font-display)', fontSize:15, fontWeight:600, marginBottom:16 }}>
            Attendance overview
          </h3>
          <div style={{ display:'flex', flexDirection:'column', gap:12 }}>
            {['Data Structures','Engineering Maths','OS','DBMS'].map((sub, i) => {
              const pct = [88, 72, 95, 80][i];
              const ok  = pct >= 75;
              return (
                <div key={sub}>
                  <div style={{ display:'flex', justifyContent:'space-between', marginBottom:4 }}>
                    <span style={{ fontSize:13, fontWeight:500 }}>{sub}</span>
                    <span style={{ fontSize:13, fontWeight:600, color: ok ? 'var(--success)' : 'var(--danger)' }}>{pct}%</span>
                  </div>
                  <div className="progress-bar">
                    <div className={`progress-fill ${ok?'green':'red'}`} style={{ width:`${pct}%` }} />
                  </div>
                  {!ok && <p style={{ fontSize:11, color:'var(--danger)', marginTop:3 }}>⚠ Below 75% — shortfall</p>}
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
}
