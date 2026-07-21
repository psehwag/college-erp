import React, { useEffect, useState, useCallback } from 'react';
import { courseAPI } from '../../services/api';

/**
 * Cascading academic selector: Department -> Course -> Semester -> Subject -> Batch.
 * Selecting an upstream field clears everything downstream.
 *
 * Props:
 *  - value: { departmentId, courseId, semester, subjectId, batchId }
 *  - onChange(next): called with the merged updated value object
 *  - onResolved({ subject, batch }): called whenever both subject & batch objects are fully resolved
 *  - showSemester, showSubject, showBatch: toggle which trailing selects render (default true)
 */
export default function AcademicCascadeSelect({
  value, onChange, onResolved,
  showSemester = true, showSubject = true, showBatch = true,
}) {
  const [depts, setDepts]       = useState([]);
  const [courses, setCourses]   = useState([]);
  const [subjects, setSubjects] = useState([]);
  const [batches, setBatches]   = useState([]);

  const v = value || {};

  useEffect(() => {
    courseAPI.getDepartments().then(r => setDepts(r.data.data || [])).catch(() => {});
  }, []);

  useEffect(() => {
    if (!v.departmentId) { setCourses([]); return; }
    courseAPI.getCoursesByDept(v.departmentId).then(r => setCourses(r.data.data || [])).catch(() => {});
  }, [v.departmentId]);

  useEffect(() => {
    if (!v.courseId || !v.semester) { setSubjects([]); return; }
    courseAPI.getSubjectsByCourse(v.courseId, v.semester).then(r => setSubjects(r.data.data || [])).catch(() => {});
  }, [v.courseId, v.semester]);

  useEffect(() => {
    if (!v.courseId) { setBatches([]); return; }
    courseAPI.getBatchesByCourse(v.courseId).then(r => {
      const list = r.data.data || [];
      // Prefer batches matching the selected semester, but don't hide others —
      // a batch's currentSemester can lag if the admin hasn't advanced it yet.
      setBatches(list);
    }).catch(() => {});
  }, [v.courseId]);

  useEffect(() => {
    if (!onResolved) return;
    const subject = subjects.find(s => String(s.id) === String(v.subjectId));
    const batch = batches.find(b => String(b.id) === String(v.batchId));
    if (subject || batch) onResolved({ subject, batch });
  }, [v.subjectId, v.batchId, subjects, batches, onResolved]);

  const set = (patch) => onChange({ ...v, ...patch });

  return (
    <div className="form-grid form-grid-3" style={{ gap: 12 }}>
      <div className="form-group">
        <label className="form-label">Department</label>
        <select className="form-select" value={v.departmentId || ''}
          onChange={e => set({ departmentId: e.target.value, courseId: '', semester: '', subjectId: '', batchId: '' })}>
          <option value="">Select department</option>
          {depts.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
        </select>
      </div>

      <div className="form-group">
        <label className="form-label">Course</label>
        <select className="form-select" value={v.courseId || ''} disabled={!v.departmentId}
          onChange={e => set({ courseId: e.target.value, semester: '', subjectId: '', batchId: '' })}>
          <option value="">Select course</option>
          {courses.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
      </div>

      {showSemester && (
        <div className="form-group">
          <label className="form-label">Semester</label>
          <select className="form-select" value={v.semester || ''} disabled={!v.courseId}
            onChange={e => set({ semester: e.target.value, subjectId: '' })}>
            <option value="">Select semester</option>
            {Array.from({ length: 8 }, (_, i) => i + 1).map(n => (
              <option key={n} value={n}>Semester {n}</option>
            ))}
          </select>
        </div>
      )}

      {showSubject && (
        <div className="form-group">
          <label className="form-label">Subject</label>
          <select className="form-select" value={v.subjectId || ''} disabled={!v.semester}
            onChange={e => set({ subjectId: e.target.value })}>
            <option value="">Select subject</option>
            {subjects.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
          </select>
        </div>
      )}

      {showBatch && (
        <div className="form-group">
          <label className="form-label">Batch</label>
          <select className="form-select" value={v.batchId || ''} disabled={!v.courseId}
            onChange={e => set({ batchId: e.target.value })}>
            <option value="">Select batch</option>
            {batches.map(b => <option key={b.id} value={b.id}>{b.name} ({b.academicYear})</option>)}
          </select>
        </div>
      )}
    </div>
  );
}
