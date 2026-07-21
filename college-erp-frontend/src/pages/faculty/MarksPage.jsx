import React, { useEffect, useState } from 'react';
import { marksAPI, studentAPI } from '../../services/api';
import AcademicCascadeSelect from '../../components/common/AcademicCascadeSelect';
import { useAuth } from '../../context/AuthContext';
import toast from 'react-hot-toast';

const GRADE_COLOR = { O:'badge-blue', A_PLUS:'badge-green', A:'badge-green', B_PLUS:'badge-green',
                      B:'badge-amber', C:'badge-amber', D:'badge-amber', F:'badge-red' };

export default function MarksPage() {
  const { user } = useAuth();
  const [sel, setSel]           = useState({});
  const [students, setStudents] = useState([]);
  const [marks, setMarks]       = useState({});   // { studentId: value }
  const [maxMarks, setMaxMarks] = useState(30);
  const [existingMarks, setExisting] = useState([]);
  const [saving, setSaving]     = useState(false);
  const [searching, setSearching] = useState(false);
  const [view, setView]         = useState('upload'); // 'upload' | 'view'

  const isReady = sel.departmentId && sel.courseId && sel.semester && sel.subjectId && sel.batchId;

  useEffect(() => {
    if (!sel.batchId) { setStudents([]); return; }
    studentAPI.getByBatch(sel.batchId).then(r => {
      const list = r.data.data || [];
      setStudents(list);
      const init = {};
      list.forEach(s => { init[s.id] = ''; });
      setMarks(init);
    }).catch(() => {});
  }, [sel.batchId]);

  const loadExisting = async () => {
    if (!isReady) return toast.error('Select department, course, semester, subject and batch');
    setSearching(true);
    try {
      const r = await marksAPI.getBatchMarks(sel.batchId, sel.subjectId);
      setExisting(r.data.data || []);
    } catch { toast.error('Failed to load marks'); }
    setSearching(false);
  };

  const submit = async () => {
    if (!isReady) return toast.error('Select department, course, semester, subject and batch');
    const studentMarks = students
      .filter(s => marks[s.id] !== '')
      .map(s => ({ studentId: s.id, marksObtained: +marks[s.id] }));
    if (studentMarks.length === 0) return toast.error('Enter at least one mark');
    setSaving(true);
    try {
      await marksAPI.bulkUpsert({
        subjectId: +sel.subjectId,
        facultyId: user?.referenceId || null,
        batchId: +sel.batchId,
        semester: +sel.semester,
        maxMarks: +maxMarks,
        studentMarks
      });
      toast.success(`Marks uploaded for ${studentMarks.length} students`);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Upload failed');
    } finally { setSaving(false); }
  };

  const pct = (obt, max) => max > 0 ? ((obt/max)*100).toFixed(1) : 0;

  return (
    <div>
      <div className="page-header">
        <div className="page-header-left">
          <h2>Marks</h2>
          <p>Upload or review marks for a batch</p>
        </div>
        <div className="tabs">
          <button className={`tab-btn ${view==='upload'?'active':''}`} onClick={() => setView('upload')}>Upload</button>
          <button className={`tab-btn ${view==='view'?'active':''}`} onClick={() => setView('view')}>View existing</button>
        </div>
      </div>

      <div className="card mb-4">
        <AcademicCascadeSelect value={sel} onChange={setSel} />
        {view === 'upload' && (
          <div className="form-group" style={{ marginTop:12, maxWidth:200 }}>
            <label className="form-label">Max marks</label>
            <input className="form-input" type="number" min={1} value={maxMarks}
              onChange={e => setMaxMarks(e.target.value)} />
          </div>
        )}
        {view === 'view' && (
          <button className="btn btn-primary" style={{ marginTop:12 }} onClick={loadExisting} disabled={searching}>
            {searching ? <span className="spinner" /> : '🔍 Search'}
          </button>
        )}
      </div>

      {!isReady ? (
        <div className="empty-state card">
          <div className="empty-icon">📝</div>
          <p>Select department, course, semester, subject and batch to continue</p>
        </div>
      ) : view === 'upload' ? (
        students.length === 0 ? (
          <div className="empty-state card"><div className="empty-icon">👥</div><p>No students found for this batch</p></div>
        ) : (
          <>
            <div className="table-wrap mb-4">
              <table>
                <thead><tr>
                  <th>Student</th><th>Enrollment</th>
                  <th>Marks obtained <span className="text-muted">/ {maxMarks}</span></th><th>%</th>
                </tr></thead>
                <tbody>
                  {students.map(s => (
                    <tr key={s.id}>
                      <td style={{ fontWeight:500 }}>{s.fullName || s.enrollmentNumber}</td>
                      <td><span className="badge badge-gray">{s.enrollmentNumber}</span></td>
                      <td style={{ width:140 }}>
                        <input className="form-input" type="number" min={0} max={maxMarks}
                          value={marks[s.id]||''} placeholder="—"
                          onChange={e => setMarks(m=>({...m,[s.id]:e.target.value}))}
                          style={{ padding:'6px 10px', width:100 }} />
                      </td>
                      <td>
                        {marks[s.id] !== '' && marks[s.id] !== undefined
                          ? <span style={{ fontWeight:500 }}>{pct(marks[s.id], maxMarks)}%</span>
                          : <span className="text-muted">—</span>}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <button className="btn btn-primary" onClick={submit} disabled={saving}>
              {saving ? <><span className="spinner"/>Saving…</> : `Upload marks for ${students.length} students`}
            </button>
          </>
        )
      ) : (
        existingMarks.length === 0 ? (
          <div className="empty-state card"><div className="empty-icon">📊</div><p>Click Search to load marks for this selection</p></div>
        ) : (
          <div className="table-wrap">
            <table>
              <thead><tr><th>Student</th><th>Enrollment</th><th>Marks</th><th>Max</th><th>%</th><th>Grade</th></tr></thead>
              <tbody>
                {existingMarks.map(m => (
                  <tr key={m.id}>
                    <td style={{ fontWeight:500 }}>{m.studentName}</td>
                    <td><span className="badge badge-gray">{m.enrollmentNumber}</span></td>
                    <td style={{ fontWeight:600 }}>{m.marksObtained}</td>
                    <td className="text-muted">{m.maxMarks}</td>
                    <td>{m.percentage != null ? `${m.percentage}%` : '—'}</td>
                    <td><span className={`badge ${GRADE_COLOR[m.grade]||'badge-gray'}`}>{m.grade || '—'}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )
      )}
    </div>
  );
}
