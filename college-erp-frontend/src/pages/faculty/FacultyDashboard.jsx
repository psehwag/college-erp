import React, { useEffect, useState } from 'react';
import { facultyAPI, courseAPI } from '../../services/api';
import { useAuth } from '../../context/AuthContext';

export default function FacultyDashboard() {
  const { user } = useAuth();
  const fid = user?.referenceId || 1;
  const [assignments, setAssignments] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    facultyAPI.getAssignments(fid)
      .then(r => setAssignments(r.data.data || []))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [fid]);

  const cards = [
    { label: 'Subjects assigned', value: assignments.length, icon: '📚', cls: 'blue' },
    { label: 'Batches', value: [...new Set(assignments.map(a=>a.batchId))].length, icon: '🎓', cls: 'green' },
    { label: 'This semester', value: assignments.filter(a=>a.isActive).length, icon: '✅', cls: 'amber' },
  ];

  return (
    <div>
      <div className="card mb-6" style={{ background:'linear-gradient(135deg, var(--primary) 0%, var(--primary-light) 100%)', border:'none', padding:'24px 28px' }}>
        <p style={{ color:'rgba(255,255,255,0.6)', fontSize:13, marginBottom:4 }}>Faculty portal</p>
        <h2 style={{ color:'#fff', fontFamily:'var(--font-display)', fontSize:22, fontWeight:700 }}>
          Good morning, {user?.username} 👋
        </h2>
        <p style={{ color:'rgba(255,255,255,0.55)', fontSize:13, marginTop:4 }}>
          {new Date().toLocaleDateString('en-IN', { weekday:'long', year:'numeric', month:'long', day:'numeric' })}
        </p>
      </div>

      <div className="grid-3 mb-6">
        {cards.map(c => (
          <div className="stat-card" key={c.label}>
            <div className={`stat-icon ${c.cls}`}>{c.icon}</div>
            <div className="stat-value">{c.value}</div>
            <div className="stat-label">{c.label}</div>
          </div>
        ))}
      </div>

      <div className="card">
        <h3 style={{ fontFamily:'var(--font-display)', fontSize:15, fontWeight:600, marginBottom:16 }}>
          Your subject assignments
        </h3>
        {loading ? <div style={{ textAlign:'center', padding:30 }}><div className="spinner spinner-dark" style={{ margin:'0 auto' }} /></div>
        : assignments.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">📚</div>
            <p>No subjects assigned yet. Contact admin.</p>
          </div>
        ) : (
          <div className="table-wrap">
            <table>
              <thead><tr><th>Subject ID</th><th>Batch ID</th><th>Semester</th><th>Academic year</th><th>Status</th></tr></thead>
              <tbody>
                {assignments.map(a => (
                  <tr key={a.id}>
                    <td>Subject #{a.subjectId}</td>
                    <td>Batch #{a.batchId}</td>
                    <td>Sem {a.semester}</td>
                    <td>{a.academicYear}</td>
                    <td><span className={`badge ${a.isActive?'badge-green':'badge-gray'}`}>{a.isActive?'Active':'Inactive'}</span></td>
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
