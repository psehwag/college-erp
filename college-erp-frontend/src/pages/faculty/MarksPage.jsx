import React, { useEffect, useState } from 'react';
import { marksAPI, courseAPI, studentAPI } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import toast from 'react-hot-toast';

const EXAM_TYPES = ['INTERNAL_1','INTERNAL_2','MIDTERM','FINAL','PRACTICAL','ASSIGNMENT','VIVA'];
const GRADE_COLOR = { O:'badge-blue', A_PLUS:'badge-green', A:'badge-green', B_PLUS:'badge-green',
                      B:'badge-amber', C:'badge-amber', D:'badge-amber', F:'badge-red' };

export default function MarksPage() {
  const { user } = useAuth();
  const [batches, setBatches]   = useState([]);
  const [subjects, setSubjects] = useState([]);
  const [students, setStudents] = useState([]);
  const [marks, setMarks]       = useState({});   // { studentId: value }
  const [sel, setSel]           = useState({ batchId:'', subjectId:'', examType:'INTERNAL_1',
                                              maxMarks:30, academicYear:'2024-25', semester:1 });
  const [existingMarks, setExisting] = useState([]);
  const [saving, setSaving]     = useState(false);
  const [view, setView]         = useState('upload'); // 'upload' | 'view'

  useEffect(() => {
    courseAPI.getDepartments()
      .then(r => Promise.all((r.data.data||[]).map(d => courseAPI.getBatchesByDept(d.id))))
      .then(rs => setBatches(rs.flatMap(r => r.data.data||[])));
  }, []);

  useEffect(() => {
    if (!sel.batchId) return;
    const batch = batches.find(b => b.id == sel.batchId);
    if (batch) {
      courseAPI.getSubjectsByCourse(batch.courseId)
        .then(r => setSubjects(r.data.data || []));
      studentAPI.getByBatch(sel.batchId)
        .then(r => {
          const list = r.data.data || [];
          setStudents(list);
          const init = {};
          list.forEach(s => { init[s.id] = ''; });
          setMarks(init);
        });
    }
  }, [sel.batchId, batches]);

  const loadExisting = async () => {
    if (!sel.batchId || !sel.subjectId) return toast.error('Select batch and subject');
    try {
      const r = await marksAPI.getBatchMarks(sel.batchId, sel.subjectId, sel.examType);
      setExisting(r.data.data || []);
      setView('view');
    } catch { toast.error('No marks found'); }
  };

  const submit = async () => {
    if (!sel.batchId || !sel.subjectId) return toast.error('Select batch and subject');
    const studentMarks = students
      .filter(s => marks[s.id] !== '')
      .map(s => ({ studentId: s.id, marksObtained: +marks[s.id] }));
    if (studentMarks.length === 0) return toast.error('Enter at least one mark');
    setSaving(true);
    try {
      await marksAPI.bulkUpsert({
        subjectId: +sel.subjectId,
        facultyId: user?.referenceId || 1,
        batchId: +sel.batchId,
        semester: +sel.semester,
        examType: sel.examType,
        maxMarks: +sel.maxMarks,
        academicYear: sel.academicYear,
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
          <h2>Upload marks</h2>
          <p>Bulk mark entry for your batch</p>
        </div>
        <div className="tabs">
          <button className={`tab-btn ${view==='upload'?'active':''}`} onClick={() => setView('upload')}>Upload</button>
          <button className={`tab-btn ${view==='view'?'active':''}`} onClick={loadExisting}>View existing</button>
        </div>
      </div>

      {/* Selectors */}
      <div className="card mb-4">
        <div className="form-grid form-grid-3" style={{ gap:12, marginBottom: sel.batchId ? 12 : 0 }}>
          <div className="form-group">
            <label className="form-label">Batch</label>
            <select className="form-select" value={sel.batchId} onChange={e => setSel(s=>({...s,batchId:e.target.value}))}>
              <option value="">Select batch</option>
              {batches.map(b=><option key={b.id} value={b.id}>{b.name}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Subject</label>
            <select className="form-select" value={sel.subjectId} onChange={e=>setSel(s=>({...s,subjectId:e.target.value}))}>
              <option value="">Select subject</option>
              {subjects.map(s=><option key={s.id} value={s.id}>{s.name}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Exam type</label>
            <select className="form-select" value={sel.examType} onChange={e=>setSel(s=>({...s,examType:e.target.value}))}>
              {EXAM_TYPES.map(t=><option key={t} value={t}>{t.replace('_',' ')}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Max marks</label>
            <input className="form-input" type="number" min={1} value={sel.maxMarks}
              onChange={e=>setSel(s=>({...s,maxMarks:e.target.value}))} />
          </div>
          <div className="form-group">
            <label className="form-label">Academic year</label>
            <input className="form-input" value={sel.academicYear}
              onChange={e=>setSel(s=>({...s,academicYear:e.target.value}))} placeholder="2024-25" />
          </div>
          <div className="form-group">
            <label className="form-label">Semester</label>
            <select className="form-select" value={sel.semester} onChange={e=>setSel(s=>({...s,semester:+e.target.value}))}>
              {[1,2,3,4,5,6,7,8].map(n=><option key={n} value={n}>Sem {n}</option>)}
            </select>
          </div>
        </div>
      </div>

      {view === 'upload' ? (
        students.length === 0 ? (
          <div className="empty-state card"><div className="empty-icon">📝</div><p>Select a batch to enter marks</p></div>
        ) : (
          <>
            <div className="table-wrap mb-4">
              <table>
                <thead><tr>
                  <th>Student</th><th>Enrollment</th>
                  <th>Marks obtained <span className="text-muted">/ {sel.maxMarks}</span></th><th>%</th>
                </tr></thead>
                <tbody>
                  {students.map(s => (
                    <tr key={s.id}>
                      <td style={{ fontWeight:500 }}>{s.fullName || s.enrollmentNumber}</td>
                      <td><span className="badge badge-gray">{s.enrollmentNumber}</span></td>
                      <td style={{ width:140 }}>
                        <input className="form-input" type="number" min={0} max={sel.maxMarks}
                          value={marks[s.id]||''} placeholder="—"
                          onChange={e => setMarks(m=>({...m,[s.id]:e.target.value}))}
                          style={{ padding:'6px 10px', width:100 }} />
                      </td>
                      <td>
                        {marks[s.id] !== '' && marks[s.id] !== undefined
                          ? <span style={{ fontWeight:500 }}>{pct(marks[s.id], sel.maxMarks)}%</span>
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
          <div className="empty-state card"><div className="empty-icon">📊</div><p>No marks found for selected criteria</p></div>
        ) : (
          <div className="table-wrap">
            <table>
              <thead><tr><th>Student ID</th><th>Marks</th><th>Max</th><th>%</th><th>Grade</th></tr></thead>
              <tbody>
                {existingMarks.map(m => (
                  <tr key={m.id}>
                    <td>{m.studentId}</td>
                    <td style={{ fontWeight:600 }}>{m.marksObtained}</td>
                    <td className="text-muted">{m.maxMarks}</td>
                    <td>{m.percentage}%</td>
                    <td><span className={`badge ${GRADE_COLOR[m.grade]||'badge-gray'}`}>{m.grade}</span></td>
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
