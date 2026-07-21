import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, BarChart, Bar } from 'recharts';
import { adminAPI } from '../../services/api';

export default function AdminDashboard() {
  const nav = useNavigate();
  const [stats, setStats]     = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    adminAPI.getDashboard()
      .then(r => setStats(r.data.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  if (loading) return (
    <div style={{ display:'flex', alignItems:'center', justifyContent:'center', height:300 }}>
      <div className="spinner spinner-dark" style={{ width:32, height:32 }} />
    </div>
  );

  const cards = [
    { label: 'Total Students', value: stats?.totalStudents ?? 0, icon: '👥', cls: 'blue', path: '/admin/students' },
    { label: 'Total Faculty',  value: stats?.totalFaculty  ?? 0, icon: '🧑‍🏫', cls: 'green', path: '/admin/faculty' },
    { label: 'Departments',    value: stats?.totalDepartments ?? 0, icon: '🏛️', cls: 'amber', path: '/admin/courses' },
    { label: 'Today Present',  value: `${stats?.todayPresent ?? 0} (${stats?.todayPercentage ?? 0}%)`, icon: '✅', cls: 'green', path: '/admin/attendance' },
  ];

  const deptStats = stats?.departmentStats || [];
  const weeklyTrend = stats?.weeklyTrend || [];
  const recentStudents = stats?.recentStudents || [];

  return (
    <div>
      {/* Stats row — each card navigates to the relevant page */}
      <div className="grid-4 mb-6">
        {cards.map(c => (
          <div className="stat-card" key={c.label} role="button" tabIndex={0}
            style={{ cursor: 'pointer' }}
            onClick={() => nav(c.path)}
            onKeyDown={e => e.key === 'Enter' && nav(c.path)}>
            <div className={`stat-icon ${c.cls}`}>{c.icon}</div>
            <div className="stat-value">{c.value}</div>
            <div className="stat-label">{c.label}</div>
          </div>
        ))}
      </div>

      {/* Charts row — real data only */}
      <div className="grid-2 mb-6">
        <div className="card">
          <h3 style={{ fontFamily:'var(--font-display)', fontSize:15, fontWeight:600, marginBottom:20 }}>
            Attendance trend — last 7 days
          </h3>
          {weeklyTrend.every(d => d.present === 0 && d.absent === 0) ? (
            <div className="empty-state" style={{ padding: 30 }}>
              <div className="empty-icon">📅</div>
              <p>No attendance has been marked yet this week.</p>
            </div>
          ) : (
            <>
              <ResponsiveContainer width="100%" height={200}>
                <LineChart data={weeklyTrend}>
                  <XAxis dataKey="day" tick={{ fontSize:12 }} axisLine={false} tickLine={false} />
                  <YAxis tick={{ fontSize:12 }} axisLine={false} tickLine={false} />
                  <Tooltip contentStyle={{ borderRadius:8, border:'1px solid #E4E7F0', fontSize:12 }} />
                  <Line type="monotone" dataKey="present" stroke="#4F6EF7" strokeWidth={2} dot={false} />
                  <Line type="monotone" dataKey="absent"  stroke="#EF4444" strokeWidth={2} dot={false} />
                </LineChart>
              </ResponsiveContainer>
              <div style={{ display:'flex', gap:16, marginTop:8 }}>
                {[['#4F6EF7','Present'],['#EF4444','Absent']].map(([c,l]) => (
                  <span key={l} style={{ display:'flex', alignItems:'center', gap:6, fontSize:12, color:'var(--text-muted)' }}>
                    <span style={{ width:8, height:8, borderRadius:'50%', background:c, display:'inline-block' }} />
                    {l}
                  </span>
                ))}
              </div>
            </>
          )}
        </div>

        <div className="card">
          <h3 style={{ fontFamily:'var(--font-display)', fontSize:15, fontWeight:600, marginBottom:20 }}>
            Students by department
          </h3>
          {deptStats.length === 0 ? (
            <div className="empty-state" style={{ padding: 30 }}>
              <div className="empty-icon">🏛️</div>
              <p>No departments yet. Add one in Courses.</p>
            </div>
          ) : (
            <ResponsiveContainer width="100%" height={200}>
              <BarChart data={deptStats.map(d => ({ name: d.code, count: d.studentCount }))}>
                <XAxis dataKey="name" tick={{ fontSize:12 }} axisLine={false} tickLine={false} />
                <YAxis tick={{ fontSize:12 }} axisLine={false} tickLine={false} allowDecimals={false} />
                <Tooltip contentStyle={{ borderRadius:8, border:'1px solid #E4E7F0', fontSize:12 }} />
                <Bar dataKey="count" fill="#4F6EF7" radius={[4,4,0,0]} />
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>
      </div>

      {/* Departments table */}
      <div className="card mb-6">
        <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:16 }}>
          <h3 style={{ fontFamily:'var(--font-display)', fontSize:15, fontWeight:600 }}>
            Departments
          </h3>
          <button className="btn btn-ghost btn-sm" onClick={() => nav('/admin/courses')}>Manage →</button>
        </div>
        {deptStats.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">🏛️</div>
            <p>No departments yet. Add one in Courses.</p>
          </div>
        ) : (
          <div className="table-wrap">
            <table>
              <thead><tr>
                <th>Name</th><th>Code</th><th>Students</th><th>Faculty</th>
              </tr></thead>
              <tbody>
                {deptStats.map(d => (
                  <tr key={d.id} style={{ cursor: 'pointer' }} onClick={() => nav('/admin/courses')}>
                    <td style={{ fontWeight:500 }}>{d.name}</td>
                    <td><span className="badge badge-blue">{d.code}</span></td>
                    <td>{d.studentCount}</td>
                    <td>{d.facultyCount}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Recently registered students */}
      <div className="card">
        <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:16 }}>
          <h3 style={{ fontFamily:'var(--font-display)', fontSize:15, fontWeight:600 }}>
            Recently registered students
          </h3>
          <button className="btn btn-ghost btn-sm" onClick={() => nav('/admin/students')}>View all →</button>
        </div>
        {recentStudents.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">👥</div>
            <p>No students registered yet.</p>
          </div>
        ) : (
          <div className="table-wrap">
            <table>
              <thead><tr><th>Name</th><th>Enrollment</th><th>Registered</th></tr></thead>
              <tbody>
                {recentStudents.map(s => (
                  <tr key={s.id} style={{ cursor: 'pointer' }} onClick={() => nav('/admin/students')}>
                    <td style={{ fontWeight:500 }}>{s.fullName}</td>
                    <td><span className="badge badge-gray">{s.enrollmentNumber}</span></td>
                    <td className="text-sm text-muted">{s.createdAt ? new Date(s.createdAt).toLocaleDateString() : '—'}</td>
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
