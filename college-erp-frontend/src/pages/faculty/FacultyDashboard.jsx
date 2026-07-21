import React, { useEffect, useState, useCallback } from 'react';
import { facultyAPI, courseAPI } from '../../services/api';
import AcademicCascadeSelect from '../../components/common/AcademicCascadeSelect';
import { useAuth } from '../../context/AuthContext';
import toast from 'react-hot-toast';

export default function FacultyDashboard() {
  const { user } = useAuth();
  const fid = user?.referenceId;
  const [assignments, setAssignments] = useState([]);
  const [subjectNames, setSubjectNames] = useState({});
  const [batchNames, setBatchNames] = useState({});
  const [loading, setLoading] = useState(true);

  // Add-assignment form state
  const [showForm, setShowForm] = useState(false);
  const [sel, setSel] = useState({});
  const [academicYear, setYear] = useState('2026-27');
  const [description, setDesc] = useState('');
  const [saving, setSaving] = useState(false);

  const load = useCallback(() => {
    if (!fid) { setLoading(false); return; }
    setLoading(true);
    facultyAPI.getAssignments(fid)
      .then(r => {
        const list = r.data.data || [];
        setAssignments(list);
        list.forEach(a => {
          courseAPI.getSubjectById(a.subjectId).then(res =>
            setSubjectNames(m => ({ ...m, [a.subjectId]: res.data.data.name }))).catch(()=>{});
          courseAPI.getBatchById(a.batchId).then(res =>
            setBatchNames(m => ({ ...m, [a.batchId]: res.data.data.name }))).catch(()=>{});
        });
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [fid]);

  useEffect(() => { load(); }, [load]);

  const addAssignment = async () => {
    if (!sel.subjectId || !sel.batchId || !sel.semester) {
      return toast.error('Select department, course, semester, subject and batch');
    }
    setSaving(true);
    try {
      await facultyAPI.assignSubject({
        facultyId: fid, subjectId: +sel.subjectId, batchId: +sel.batchId,
        semester: +sel.semester, academicYear, description,
      });
      toast.success('Assignment created');
      setSel({}); setDesc(''); setShowForm(false);
      load();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to create assignment');
    } finally { setSaving(false); }
  };

  const removeAssignment = async id => {
    if (!window.confirm('Delete this assignment?')) return;
    try { await facultyAPI.removeAssignment(id); toast.success('Assignment deleted'); load(); }
    catch (err) { toast.error(err.response?.data?.message || 'Failed'); }
  };

  const displayName = user?.name || user?.username || 'Faculty';

  const cards = [
    { label: 'Subjects assigned', value: assignments.length, icon: '📚', cls: 'blue' },
    { label: 'Batches', value: [...new Set(assignments.map(a=>a.batchId))].length, icon: '🎓', cls: 'green' },
  ];

  return (
    <div>
      <div className="card mb-6" style={{ background:'linear-gradient(135deg, var(--primary) 0%, var(--primary-light) 100%)', border:'none', padding:'24px 28px' }}>
        <p style={{ color:'rgba(255,255,255,0.6)', fontSize:13, marginBottom:4 }}>Faculty portal</p>
        <h2 style={{ color:'#fff', fontFamily:'var(--font-display)', fontSize:22, fontWeight:700 }}>
          Good morning, {displayName} 👋
        </h2>
        <p style={{ color:'rgba(255,255,255,0.55)', fontSize:13, marginTop:4 }}>
          {new Date().toLocaleDateString('en-IN', { weekday:'long', year:'numeric', month:'long', day:'numeric' })}
        </p>
      </div>

      <div className="grid-2 mb-6">
        {cards.map(c => (
          <div className="stat-card" key={c.label}>
            <div className={`stat-icon ${c.cls}`}>{c.icon}</div>
            <div className="stat-value">{c.value}</div>
            <div className="stat-label">{c.label}</div>
          </div>
        ))}
      </div>

      <div className="card">
        <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:16 }}>
          <h3 style={{ fontFamily:'var(--font-display)', fontSize:15, fontWeight:600 }}>
            Your subject assignments
          </h3>
          <button className="btn btn-primary btn-sm" onClick={() => setShowForm(f => !f)}>
            {showForm ? 'Cancel' : '+ New assignment'}
          </button>
        </div>

        {showForm && (
          <div className="card" style={{ background:'var(--bg)', marginBottom:20 }}>
            <AcademicCascadeSelect value={sel} onChange={setSel} />
            <div className="form-grid form-grid-2" style={{ gap:12, marginTop:12 }}>
              <div className="form-group">
                <label className="form-label">Academic year</label>
                <input className="form-input" value={academicYear} onChange={e => setYear(e.target.value)} placeholder="2026-27" />
              </div>
              <div className="form-group">
                <label className="form-label">Description (optional)</label>
                <input className="form-input" value={description} onChange={e => setDesc(e.target.value)}
                  placeholder="e.g. Covers units 1-5, includes lab sessions" />
              </div>
            </div>
            <button className="btn btn-primary" onClick={addAssignment} disabled={saving} style={{ marginTop:10 }}>
              {saving ? <span className="spinner" /> : 'Create assignment'}
            </button>
          </div>
        )}

        {loading ? <div style={{ textAlign:'center', padding:30 }}><div className="spinner spinner-dark" style={{ margin:'0 auto' }} /></div>
        : assignments.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">📚</div>
            <p>No subjects assigned yet. Click "New assignment" to add one.</p>
          </div>
        ) : (
          <div className="table-wrap">
            <table>
              <thead><tr><th>Subject</th><th>Batch</th><th>Semester</th><th>Academic year</th><th>Description</th><th></th></tr></thead>
              <tbody>
                {assignments.map(a => (
                  <tr key={a.id}>
                    <td style={{ fontWeight:500 }}>{subjectNames[a.subjectId] || `Subject #${a.subjectId}`}</td>
                    <td>{batchNames[a.batchId] || `Batch #${a.batchId}`}</td>
                    <td>Sem {a.semester}</td>
                    <td className="text-sm text-muted">{a.academicYear}</td>
                    <td className="text-sm">{a.description || '—'}</td>
                    <td>
                      <button className="btn btn-ghost btn-sm" style={{ color:'var(--danger)' }}
                        onClick={() => removeAssignment(a.id)}>Delete</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
