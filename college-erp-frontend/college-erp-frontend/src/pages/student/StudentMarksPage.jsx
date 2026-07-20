import React, { useEffect, useState } from 'react';
import { marksAPI } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import toast from 'react-hot-toast';

const GRADE_COLOR = { O:'badge-blue', A_PLUS:'badge-green', A:'badge-green',
  B_PLUS:'badge-amber', B:'badge-amber', C:'badge-amber', D:'badge-gray', F:'badge-red' };

export default function StudentMarksPage() {
  const { user } = useAuth();
  const studentId = user?.referenceId;

  const [marks, setMarks]       = useState([]);
  const [semesters, setSems]    = useState([]);
  const [activeSem, setActiveSem] = useState(null);
  const [summary, setSummary]   = useState(null);
  const [loading, setLoading]   = useState(true);

  useEffect(() => {
    if (!studentId) return;
    marksAPI.getByStudent(studentId)
      .then(r => {
        const list = r.data.data || [];
        setMarks(list);
        const unique = [...new Set(list.map(m => m.semester))].sort((a,b)=>a-b);
        setSems(unique);
        if (unique.length) setActiveSem(unique[unique.length - 1]);
      })
      .catch(() => toast.error('Failed to load marks'))
      .finally(() => setLoading(false));
  }, [studentId]);

  useEffect(() => {
    if (!studentId || !activeSem) return;
    marksAPI.getSemSummary(studentId, activeSem)
      .then(r => setSummary(r.data.data))
      .catch(() => {});
  }, [studentId, activeSem]);

  const filtered = marks.filter(m => m.semester === activeSem);

  if (loading) return (
    <div style={{ textAlign:'center', padding:60 }}>
      <div className="spinner spinner-dark" style={{ margin:'0 auto' }} />
    </div>
  );

  return (
    <div>
      <div className="page-header">
        <div className="page-header-left">
          <h2>My Marks</h2>
          <p>All exam results with grades</p>
        </div>
      </div>

      {semesters.length === 0 ? (
        <div className="empty-state card">
          <div className="empty-icon">📊</div>
          <p>No marks uploaded yet.</p>
        </div>
      ) : (
        <>
          {/* Semester tabs */}
          <div className="tabs" style={{ marginBottom:20 }}>
            {semesters.map(s => (
              <button key={s} className={`tab-btn ${activeSem===s?'active':''}`}
                onClick={() => setActiveSem(s)}>Sem {s}</button>
            ))}
          </div>

          {/* Summary card */}
          {summary && (
            <div style={{ display:'grid', gridTemplateColumns:'repeat(auto-fill, minmax(160px,1fr))', gap:12, marginBottom:20 }}>
              {[
                ['Total marks', `${summary.totalObtained} / ${summary.totalMax}`],
                ['Overall %', `${summary.overallPercentage?.toFixed(1)}%`],
              ].map(([label, val]) => (
                <div key={label} className="card" style={{ textAlign:'center' }}>
                  <div style={{ fontSize:11, color:'var(--text-muted)', textTransform:'uppercase',
                    letterSpacing:'0.5px', marginBottom:6 }}>{label}</div>
                  <div style={{ fontFamily:'var(--font-display)', fontSize:22, fontWeight:700 }}>{val}</div>
                </div>
              ))}
            </div>
          )}

          {/* Marks table */}
          <div className="table-wrap">
            <table>
              <thead><tr>
                <th>Subject</th><th>Exam type</th><th>Marks</th><th>Max</th><th>%</th><th>Grade</th>
              </tr></thead>
              <tbody>
                {filtered.map(m => {
                  const pct = m.maxMarks ? ((m.marksObtained/m.maxMarks)*100).toFixed(1) : '—';
                  return (
                    <tr key={m.id}>
                      <td>Subject #{m.subjectId}</td>
                      <td><span className="badge badge-gray text-sm">{m.examType?.replace('_',' ')}</span></td>
                      <td style={{ fontWeight:600 }}>{m.marksObtained}</td>
                      <td className="text-muted">{m.maxMarks}</td>
                      <td>{pct}%</td>
                      <td>
                        <span className={`badge ${GRADE_COLOR[m.grade]||'badge-gray'}`}>
                          {m.grade || '—'}
                        </span>
                      </td>
                    </tr>
                  );
                })}
                {filtered.length === 0 && (
                  <tr><td colSpan={6} style={{ textAlign:'center', color:'var(--text-muted)', padding:24 }}>
                    No marks for Semester {activeSem}
                  </td></tr>
                )}
              </tbody>
            </table>
          </div>
        </>
      )}
    </div>
  );
}
