import React, { useState } from 'react';
import { adminAPI, studentAPI } from '../../services/api';
import AcademicCascadeSelect from '../../components/common/AcademicCascadeSelect';
import toast from 'react-hot-toast';

export default function DefaultersPage() {
  const [sel, setSel]               = useState({});
  const [threshold, setThreshold]   = useState(75);
  const [defaulters, setDefaulters] = useState([]);
  const [studentMap, setStudentMap] = useState({});
  const [loading, setLoading]       = useState(false);
  const [searched, setSearched]     = useState(false);

  const isReady = sel.departmentId && sel.courseId && sel.semester && sel.subjectId && sel.batchId;

  const run = async () => {
    if (!isReady) return toast.error('Select department, course, semester, subject and batch');
    setLoading(true);
    setSearched(true);
    try {
      const res = await adminAPI.getDefaulters(sel.subjectId, sel.batchId, threshold);
      const ids = res.data.data || [];
      setDefaulters(ids);

      const map = {};
      await Promise.all(ids.map(async id => {
        try {
          const s = await studentAPI.getById(id);
          map[id] = s.data.data;
        } catch { map[id] = { fullName: `Student #${id}`, enrollmentNumber: '—' }; }
      }));
      setStudentMap(map);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to load defaulters');
    }
    setLoading(false);
  };

  return (
    <div>
      <div className="page-header">
        <div className="page-header-left">
          <h2>Defaulter report</h2>
          <p>Students below the attendance threshold, scoped to one batch</p>
        </div>
      </div>

      <div className="card mb-4">
        <AcademicCascadeSelect value={sel} onChange={(v) => { setSel(v); setSearched(false); }} />
        <div className="form-group" style={{ marginTop:12, maxWidth:200 }}>
          <label className="form-label">Threshold (%)</label>
          <input className="form-input" type="number" min="1" max="100"
            value={threshold} onChange={e => setThreshold(+e.target.value)} />
        </div>
        <button className="btn btn-primary" onClick={run} disabled={loading || !isReady} style={{ marginTop:12 }}>
          {loading ? <span className="spinner" /> : '🔍 Generate report'}
        </button>
      </div>

      {!isReady ? (
        <div className="empty-state card">
          <div className="empty-icon">📋</div>
          <p>Select department, course, semester, subject and batch to continue</p>
        </div>
      ) : searched && defaulters.length > 0 ? (
        <div className="card">
          <div style={{ display:'flex', justifyContent:'space-between', marginBottom:16 }}>
            <h3 style={{ fontFamily:'var(--font-display)', fontSize:15, fontWeight:600 }}>
              Defaulters — {defaulters.length} student{defaulters.length!==1?'s':''}
            </h3>
            <span className="badge badge-red">Below {threshold}%</span>
          </div>
          <div className="table-wrap">
            <table>
              <thead><tr><th>Enrollment</th><th>Name</th><th>Email</th></tr></thead>
              <tbody>
                {defaulters.map(id => {
                  const s = studentMap[id] || {};
                  return (
                    <tr key={id}>
                      <td><span className="badge badge-gray">{s.enrollmentNumber || id}</span></td>
                      <td style={{ fontWeight:500 }}>{s.fullName || `Student #${id}`}</td>
                      <td className="text-sm text-muted">{s.email || '—'}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      ) : searched && !loading ? (
        <div className="empty-state card">
          <div className="empty-icon">🎉</div>
          <p>No defaulters found. All students are above {threshold}%.</p>
        </div>
      ) : null}
    </div>
  );
}
