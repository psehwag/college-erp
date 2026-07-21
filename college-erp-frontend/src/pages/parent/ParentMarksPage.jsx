import React, { useEffect, useState } from 'react';
import { studentAPI, marksAPI } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import useSubjectMap from '../../hooks/useSubjectMap';
import toast from 'react-hot-toast';

const GRADE_COLOR = { O:'badge-blue', A_PLUS:'badge-green', A:'badge-green',
  B_PLUS:'badge-amber', B:'badge-amber', C:'badge-amber', D:'badge-gray', F:'badge-red' };

export default function ParentMarksPage() {
  const { user } = useAuth();
  const parentId = user?.referenceId;
  const { getName, ensureLoaded } = useSubjectMap();

  const [children, setChildren] = useState([]);
  const [selected, setSelected] = useState(null);
  const [marks, setMarks]       = useState([]);
  const [semesters, setSems]    = useState([]);
  const [activeSem, setActiveSem] = useState(null);
  const [summary, setSummary]   = useState(null);
  const [loading, setLoading]   = useState(true);
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
    marksAPI.getByStudent(selected.id)
      .then(r => {
        const list = r.data.data || [];
        setMarks(list);
        ensureLoaded(list.map(m => m.subjectId));
        const unique = [...new Set(list.map(m => m.semester))].sort((a,b)=>a-b);
        setSems(unique);
        setActiveSem(unique.length ? unique[unique.length - 1] : null);
      })
      .catch(() => toast.error('Failed to load marks'))
      .finally(() => setLoadingData(false));
  }, [selected]);

  useEffect(() => {
    if (!selected || !activeSem) { setSummary(null); return; }
    marksAPI.getSemSummary(selected.id, activeSem).then(r => setSummary(r.data.data)).catch(() => {});
  }, [selected, activeSem]);

  const filtered = marks.filter(m => m.semester === activeSem);

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
          <h2>Child's Marks</h2>
          <p>All exam results with grades</p>
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
      ) : semesters.length === 0 ? (
        <div className="empty-state card">
          <div className="empty-icon">📊</div>
          <p>No marks uploaded yet.</p>
        </div>
      ) : (
        <>
          <div className="tabs" style={{ marginBottom:20 }}>
            {semesters.map(s => (
              <button key={s} className={`tab-btn ${activeSem===s?'active':''}`}
                onClick={() => setActiveSem(s)}>Sem {s}</button>
            ))}
          </div>

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

          <div className="table-wrap">
            <table>
              <thead><tr><th>Subject</th><th>Marks</th><th>Max</th><th>%</th><th>Grade</th></tr></thead>
              <tbody>
                {filtered.map(m => (
                  <tr key={m.id}>
                    <td style={{ fontWeight:500 }}>{getName(m.subjectId)}</td>
                    <td style={{ fontWeight:600 }}>{m.marksObtained}</td>
                    <td className="text-muted">{m.maxMarks}</td>
                    <td>{m.percentage != null ? `${m.percentage}%` : '—'}</td>
                    <td><span className={`badge ${GRADE_COLOR[m.grade]||'badge-gray'}`}>{m.grade || '—'}</span></td>
                  </tr>
                ))}
                {filtered.length === 0 && (
                  <tr><td colSpan={5} style={{ textAlign:'center', color:'var(--text-muted)', padding:24 }}>
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
