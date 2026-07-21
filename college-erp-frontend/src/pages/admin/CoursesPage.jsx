import React, { useEffect, useState } from 'react';
import { courseAPI } from '../../services/api';
import toast from 'react-hot-toast';

export default function CoursesPage() {
  const [tab, setTab]       = useState('departments');
  const [depts, setDepts]   = useState([]);
  const [courses, setCourses] = useState([]);
  const [subjects, setSubjects] = useState([]);
  const [batches, setBatches] = useState([]);
  const [selDept, setSelDept] = useState('');
  const [selCourse, setSelCourse] = useState('');
  const [modal, setModal]   = useState(null);
  const [loading, setLoading] = useState(true);

  // Admin sees inactive records too, so they can be reactivated or removed
  const loadDepts = () => courseAPI.getDepartments(true).then(r => { setDepts(r.data.data||[]); setLoading(false); });
  const loadCourses = () => selDept && courseAPI.getCoursesByDept(selDept, true).then(r => setCourses(r.data.data||[]));
  const loadBatches = () => selDept && courseAPI.getBatchesByDept(selDept, true).then(r => setBatches(r.data.data||[]));
  const loadSubjects = () => selCourse && courseAPI.getSubjectsByCourse(selCourse, null, true).then(r => setSubjects(r.data.data||[]));

  useEffect(() => { loadDepts(); }, []);
  useEffect(() => { loadCourses(); loadBatches(); }, [selDept]);
  useEffect(() => { loadSubjects(); }, [selCourse]);

  const TABS = ['departments','courses','subjects','batches'];

  // ── Delete / activate handlers ────────────────────────────────────────

  const deleteDept = async (d) => {
    if (!window.confirm(
      `PERMANENTLY delete "${d.name}"? This also deletes every course, subject, ` +
      `batch, student and faculty under this department. This cannot be undone.`
    )) return;
    try { await courseAPI.deleteDept(d.id); toast.success('Department and all its data deleted'); loadDepts(); }
    catch (err) { toast.error(err.response?.data?.message || 'Failed to delete'); }
  };
  const toggleDept = async (d) => {
    try { await courseAPI.setDeptActive(d.id, !d.isActive); toast.success(d.isActive ? 'Department deactivated' : 'Department activated'); loadDepts(); }
    catch (err) { toast.error(err.response?.data?.message || 'Failed'); }
  };

  const deleteCourse = async (c) => {
    if (!window.confirm(`PERMANENTLY delete "${c.name}"? This also deletes its subjects, batches and students. Cannot be undone.`)) return;
    try { await courseAPI.deleteCourse(c.id); toast.success('Course and all its data deleted'); loadCourses(); }
    catch (err) { toast.error(err.response?.data?.message || 'Failed to delete'); }
  };
  const toggleCourse = async (c) => {
    try { await courseAPI.setCourseActive(c.id, !c.isActive); toast.success(c.isActive ? 'Course deactivated' : 'Course activated'); loadCourses(); }
    catch (err) { toast.error(err.response?.data?.message || 'Failed'); }
  };

  const deleteSubject = async (s) => {
    if (!window.confirm(`PERMANENTLY delete "${s.name}"? This also deletes its attendance/marks history. Cannot be undone.`)) return;
    try { await courseAPI.deleteSubject(s.id); toast.success('Subject and its history deleted'); loadSubjects(); }
    catch (err) { toast.error(err.response?.data?.message || 'Failed to delete'); }
  };
  const toggleSubject = async (s) => {
    try { await courseAPI.setSubjectActive(s.id, !s.isActive); toast.success(s.isActive ? 'Subject deactivated' : 'Subject activated'); loadSubjects(); }
    catch (err) { toast.error(err.response?.data?.message || 'Failed'); }
  };

  const deleteBatch = async (b) => {
    if (!window.confirm(`PERMANENTLY delete "${b.name}"? This also deletes every student enrolled in it. Cannot be undone.`)) return;
    try { await courseAPI.deleteBatch(b.id); toast.success('Batch and its students deleted'); loadBatches(); }
    catch (err) { toast.error(err.response?.data?.message || 'Failed to delete'); }
  };
  const toggleBatch = async (b) => {
    try { await courseAPI.setBatchActive(b.id, !b.isActive); toast.success(b.isActive ? 'Batch deactivated' : 'Batch activated'); loadBatches(); }
    catch (err) { toast.error(err.response?.data?.message || 'Failed'); }
  };

  return (
    <div>
      <div className="page-header">
        <div className="page-header-left">
          <h2>Courses & subjects</h2>
          <p>Manage departments, courses, subjects and batches</p>
        </div>
      </div>

      <div className="tabs mb-6">
        {TABS.map(t => <button key={t} className={`tab-btn ${tab===t?'active':''}`}
          onClick={() => setTab(t)}>{t.charAt(0).toUpperCase()+t.slice(1)}</button>)}
      </div>

      {(tab==='courses'||tab==='subjects'||tab==='batches') && (
        <div className="card mb-4" style={{ display:'flex', gap:12, alignItems:'flex-end', flexWrap:'wrap' }}>
          <div className="form-group" style={{ minWidth:200 }}>
            <label className="form-label">Department</label>
            <select className="form-select" value={selDept} onChange={e => setSelDept(e.target.value)}>
              <option value="">Select department</option>
              {depts.map(d => <option key={d.id} value={d.id}>{d.name}{!d.isActive ? ' (inactive)' : ''}</option>)}
            </select>
          </div>
          {tab==='subjects' && (
            <div className="form-group" style={{ minWidth:200 }}>
              <label className="form-label">Course</label>
              <select className="form-select" value={selCourse} onChange={e => setSelCourse(e.target.value)}>
                <option value="">Select course</option>
                {courses.map(c => <option key={c.id} value={c.id}>{c.name}{!c.isActive ? ' (inactive)' : ''}</option>)}
              </select>
            </div>
          )}
        </div>
      )}

      {/* Departments tab */}
      {tab === 'departments' && (
        <>
          <div style={{ display:'flex', justifyContent:'flex-end', marginBottom:12 }}>
            <button className="btn btn-primary" onClick={() => setModal('dept')}>+ Add department</button>
          </div>
          {loading ? <div className="empty-state"><div className="spinner spinner-dark" style={{ margin:'0 auto' }} /></div>
          : depts.length === 0 ? <div className="empty-state card"><div className="empty-icon">🏛️</div><p>No departments yet</p></div>
          : (
            <div className="grid-3">
              {depts.map(d => (
                <div key={d.id} className="card">
                  <div style={{ display:'flex', justifyContent:'space-between', marginBottom:8 }}>
                    <span className="badge badge-blue">{d.code}</span>
                    <span className={`badge ${d.isActive?'badge-green':'badge-gray'}`}>{d.isActive?'Active':'Inactive'}</span>
                  </div>
                  <h4 style={{ fontFamily:'var(--font-display)', fontSize:15, fontWeight:600 }}>{d.name}</h4>
                  <p style={{ fontSize:12.5, color:'var(--text-muted)', marginTop:4, marginBottom:12 }}>{d.description || 'No description'}</p>
                  <div style={{ display:'flex', gap:6 }}>
                    <button className="btn btn-secondary btn-sm" onClick={() => toggleDept(d)}>
                      {d.isActive ? 'Deactivate' : 'Activate'}
                    </button>
                    <button className="btn btn-ghost btn-sm" style={{ color:'var(--danger)' }} onClick={() => deleteDept(d)}>
                      Delete
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </>
      )}

      {/* Courses tab */}
      {tab === 'courses' && (
        <>
          <div style={{ display:'flex', justifyContent:'flex-end', marginBottom:12 }}>
            <button className="btn btn-primary" onClick={() => setModal('course')} disabled={!selDept}>+ Add course</button>
          </div>
          {courses.length === 0
            ? <div className="empty-state card"><div className="empty-icon">📚</div><p>{selDept?'No courses for this department':'Select a department'}</p></div>
            : <div className="grid-3">
              {courses.map(c => (
                <div key={c.id} className="card">
                  <div style={{ display:'flex', justifyContent:'space-between', marginBottom:8 }}>
                    <span className="badge badge-blue">{c.code}</span>
                    <span className={`badge ${c.isActive?'badge-green':'badge-gray'}`}>{c.isActive?'Active':'Inactive'}</span>
                  </div>
                  <h4 style={{ fontFamily:'var(--font-display)', fontSize:14, fontWeight:600 }}>{c.name}</h4>
                  <p style={{ fontSize:12, color:'var(--text-muted)', marginTop:4, marginBottom:12 }}>{c.totalSemesters} semesters · {c.durationYears} years</p>
                  <div style={{ display:'flex', gap:6 }}>
                    <button className="btn btn-secondary btn-sm" onClick={() => toggleCourse(c)}>
                      {c.isActive ? 'Deactivate' : 'Activate'}
                    </button>
                    <button className="btn btn-ghost btn-sm" style={{ color:'var(--danger)' }} onClick={() => deleteCourse(c)}>
                      Delete
                    </button>
                  </div>
                </div>
              ))}
            </div>}
        </>
      )}

      {/* Subjects tab — "Type" column removed per requirements */}
      {tab === 'subjects' && (
        <>
          <div style={{ display:'flex', justifyContent:'flex-end', marginBottom:12 }}>
            <button className="btn btn-primary" onClick={() => setModal('subject')} disabled={!selCourse}>+ Add subject</button>
          </div>
          {subjects.length === 0
            ? <div className="empty-state card"><div className="empty-icon">📖</div><p>{selCourse?'No subjects for this course':'Select a department and course'}</p></div>
            : <div className="table-wrap"><table>
              <thead><tr><th>Code</th><th>Name</th><th>Semester</th><th>Credits</th><th>Lectures</th><th>Status</th><th></th></tr></thead>
              <tbody>
                {subjects.map(s => (
                  <tr key={s.id}>
                    <td><span className="badge badge-gray">{s.code}</span></td>
                    <td style={{ fontWeight:500 }}>{s.name}</td>
                    <td>Sem {s.semester}</td>
                    <td>{s.credits}</td>
                    <td>{s.totalLectures}</td>
                    <td><span className={`badge ${s.isActive?'badge-green':'badge-gray'}`}>{s.isActive?'Active':'Inactive'}</span></td>
                    <td>
                      <div style={{ display:'flex', gap:6 }}>
                        <button className="btn btn-ghost btn-sm" onClick={() => toggleSubject(s)}>
                          {s.isActive ? 'Deactivate' : 'Activate'}
                        </button>
                        <button className="btn btn-ghost btn-sm" style={{ color:'var(--danger)' }} onClick={() => deleteSubject(s)}>
                          Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table></div>}
        </>
      )}

      {/* Batches tab */}
      {tab === 'batches' && (
        <>
          <div style={{ display:'flex', justifyContent:'flex-end', marginBottom:12 }}>
            <button className="btn btn-primary" onClick={() => setModal('batch')} disabled={!selDept}>+ Add batch</button>
          </div>
          {batches.length === 0
            ? <div className="empty-state card"><div className="empty-icon">🎓</div><p>{selDept?'No batches for this department':'Select a department'}</p></div>
            : <div className="table-wrap"><table>
              <thead><tr><th>Batch</th><th>Academic year</th><th>Semester</th><th>Max strength</th><th>Status</th><th></th></tr></thead>
              <tbody>
                {batches.map(b => (
                  <tr key={b.id}>
                    <td style={{ fontWeight:500 }}>{b.name}</td>
                    <td>{b.academicYear}</td>
                    <td>Sem {b.currentSemester}</td>
                    <td>{b.maxStrength}</td>
                    <td><span className={`badge ${b.isActive?'badge-green':'badge-gray'}`}>{b.isActive?'Active':'Inactive'}</span></td>
                    <td>
                      <div style={{ display:'flex', gap:6 }}>
                        <button className="btn btn-ghost btn-sm" onClick={() => toggleBatch(b)}>
                          {b.isActive ? 'Deactivate' : 'Activate'}
                        </button>
                        <button className="btn btn-ghost btn-sm" style={{ color:'var(--danger)' }} onClick={() => deleteBatch(b)}>
                          Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table></div>}
        </>
      )}

      {modal && (
        <QuickAddModal type={modal} depts={depts} courses={courses} selDept={selDept} selCourse={selCourse}
          onClose={() => setModal(null)}
          onSaved={() => { setModal(null); loadDepts(); loadCourses(); loadSubjects(); loadBatches(); }} />
      )}
    </div>
  );
}

function QuickAddModal({ type, depts, courses, selDept, selCourse, onClose, onSaved }) {
  const [form, setForm] = useState({
    departmentId: selDept || '', courseId: selCourse || '',
  });
  const [saving, setSaving] = useState(false);
  const set = (k,v) => setForm(f=>({...f,[k]:v}));

  const submit = async e => {
    e.preventDefault();
    setSaving(true);
    try {
      if (type==='dept') await courseAPI.createDept(form);
      else if (type==='course') await courseAPI.createCourse(form);
      else if (type==='subject') await courseAPI.createSubject(form);
      else if (type==='batch') await courseAPI.createBatch(form);
      toast.success(`${type.charAt(0).toUpperCase()+type.slice(1)} created`);
      onSaved();
    } catch (err) { toast.error(err.response?.data?.message||'Failed'); }
    finally { setSaving(false); }
  };

  const FIELDS = {
    dept: [['name','Name','text'],['code','Code (e.g. CS)','text'],['description','Description','text']],
    course: [['name','Course name','text'],['code','Code','text'],['totalSemesters','Semesters','number'],['durationYears','Duration (years)','number']],
    subject: [['name','Subject name','text'],['code','Code','text'],['semester','Semester','number'],['credits','Credits','number'],['totalLectures','Total lectures','number']],
    batch: [['name','Batch name','text'],['academicYear','Academic year (e.g. 2026-27)','text'],['currentSemester','Current semester','number'],['maxStrength','Max strength','number']],
  };

  return (
    <div className="modal-overlay" onClick={e => e.target===e.currentTarget && onClose()}>
      <div className="modal">
        <div className="modal-header">
          <h3>Add {type}</h3>
          <button className="modal-close" onClick={onClose}>✕</button>
        </div>
        <form onSubmit={submit}>
          <div className="modal-body">
            <div style={{ display:'flex', flexDirection:'column', gap:14 }}>
              {(type==='course'||type==='subject'||type==='batch') && (
                <div className="form-group">
                  <label className="form-label">Department</label>
                  <select className="form-select" value={form.departmentId||''} onChange={e=>set('departmentId',+e.target.value)} required>
                    <option value="">Select</option>
                    {depts.map(d=><option key={d.id} value={d.id}>{d.name}</option>)}
                  </select>
                </div>
              )}
              {(type==='subject'||type==='batch') && courses.length>0 && (
                <div className="form-group">
                  <label className="form-label">Course</label>
                  <select className="form-select" value={form.courseId||''} onChange={e=>set('courseId',+e.target.value)}>
                    <option value="">Select</option>
                    {courses.map(c=><option key={c.id} value={c.id}>{c.name}</option>)}
                  </select>
                </div>
              )}
              {FIELDS[type]?.map(([k,l,t]) => (
                <div className="form-group" key={k}>
                  <label className="form-label">{l}</label>
                  <input className="form-input" type={t} value={form[k]||''}
                    onChange={e=>set(k,t==='number'?+e.target.value:e.target.value)} required />
                </div>
              ))}
            </div>
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving?<span className="spinner"/>:'Create'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
