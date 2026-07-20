import React, { useEffect, useState } from 'react';
import { courseAPI, attendanceAPI, studentAPI } from '../../services/api';
import toast from 'react-hot-toast';

export default function DefaultersPage() {
  const [subjects, setSubjects]     = useState([]);
  const [batches, setBatches]       = useState([]);
  const [selectedBatch, setBatch]   = useState('');
  const [selectedSubject, setSubject] = useState('');
  const [threshold, setThreshold]   = useState(75);
  const [defaulters, setDefaulters] = useState([]);
  const [studentMap, setStudentMap] = useState({});
  const [loading, setLoading]       = useState(false);

  useEffect(() => {
    courseAPI.getDepartments()
      .then(r => r.data.data || [])
      .then(depts => Promise.all(depts.map(d => courseAPI.getBatchesByDept(d.id))))
      .then(results => setBatches(results.flatMap(r => r.data.data || [])))
      .catch(()=>{});
  }, []);

  useEffect(() => {
    if (!selectedBatch) return;
    const batch = batches.find(b => b.id == selectedBatch);
    if (batch) {
      courseAPI.getSubjectsByCourse(batch.courseId, batch.currentSemester)
        .then(r => setSubjects(r.data.data || [])).catch(()=>{});
    }
  }, [selectedBatch, batches]);

  const run = async () => {
    if (!selectedSubject) return toast.error('Select a subject');
    setLoading(true);
    try {
      const res = await attendanceAPI.getDefaulters(+selectedSubject, threshold);
      const ids = res.data.data || [];
      setDefaulters(ids);

      // Fetch student names for IDs
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
          <p>Students below the attendance threshold</p>
        </div>
      </div>

      <div className="card mb-4">
        <div className="form-grid form-grid-3">
          <div className="form-group">
            <label className="form-label">Batch</label>
            <select className="form-select" value={selectedBatch}
              onChange={e => { setBatch(e.target.value); setSubject(''); setDefaulters([]); }}>
              <option value="">Select batch</option>
              {batches.map(b => <option key={b.id} value={b.id}>{b.name}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Subject</label>
            <select className="form-select" value={selectedSubject}
              onChange={e => setSubject(e.target.value)}>
              <option value="">Select subject</option>
              {subjects.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Threshold (%)</label>
            <input className="form-input" type="number" min="1" max="100"
              value={threshold} onChange={e => setThreshold(+e.target.value)} />
          </div>
        </div>
        <button className="btn btn-primary" onClick={run} disabled={loading}>
          {loading ? <span className="spinner" /> : '🔍 Generate report'}
        </button>
      </div>

      {defaulters.length > 0 ? (
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
      ) : !loading && selectedSubject ? (
        <div className="empty-state card">
          <div className="empty-icon">🎉</div>
          <p>No defaulters found. All students are above {threshold}%.</p>
        </div>
      ) : null}
    </div>
  );
}
