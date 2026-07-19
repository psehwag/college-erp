import React, { useEffect, useState } from 'react';
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, BarChart, Bar } from 'recharts';
import { adminAPI, studentAPI, facultyAPI, courseAPI } from '../../services/api';

const MOCK_TREND = [
  { day: 'Mon', present: 182, absent: 18 },
  { day: 'Tue', present: 190, absent: 10 },
  { day: 'Wed', present: 175, absent: 25 },
  { day: 'Thu', present: 188, absent: 12 },
  { day: 'Fri', present: 195, absent: 5 },
  { day: 'Sat', present: 160, absent: 40 },
];

export default function AdminDashboard() {
  const [stats, setStats]   = useState(null);
  const [depts, setDepts]   = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.allSettled([
      adminAPI.getDashboard(),
      courseAPI.getDepartments(),
    ]).then(([s, d]) => {
      if (s.status === 'fulfilled') setStats(s.value.data.data);
      if (d.status === 'fulfilled') setDepts(d.value.data.data || []);
      setLoading(false);
    });
  }, []);

  if (loading) return (
    <div style={{ display:'flex', alignItems:'center', justifyContent:'center', height:300 }}>
      <div className="spinner spinner-dark" style={{ width:32, height:32 }} />
    </div>
  );

  const cards = [
    { label: 'Total Students', value: stats?.totalStudents ?? '—', icon: '👥', cls: 'blue' },
    { label: 'Total Faculty',  value: stats?.totalFaculty  ?? '—', icon: '🧑‍🏫', cls: 'green' },
    { label: 'Departments',    value: depts.length,                 icon: '🏛️', cls: 'amber' },
    { label: 'Today Present',  value: stats?.todayPresent  ?? '—', icon: '✅', cls: 'green' },
  ];

  return (
    <div>
      {/* Stats row */}
      <div className="grid-4 mb-6">
        {cards.map(c => (
          <div className="stat-card" key={c.label}>
            <div className={`stat-icon ${c.cls}`}>{c.icon}</div>
            <div className="stat-value">{c.value}</div>
            <div className="stat-label">{c.label}</div>
          </div>
        ))}
      </div>

      {/* Charts row */}
      <div className="grid-2 mb-6">
        <div className="card">
          <h3 style={{ fontFamily:'var(--font-display)', fontSize:15, fontWeight:600, marginBottom:20 }}>
            Attendance trend — this week
          </h3>
          <ResponsiveContainer width="100%" height={200}>
            <LineChart data={MOCK_TREND}>
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
        </div>

        <div className="card">
          <h3 style={{ fontFamily:'var(--font-display)', fontSize:15, fontWeight:600, marginBottom:20 }}>
            Students by department
          </h3>
          <ResponsiveContainer width="100%" height={200}>
            <BarChart data={depts.map(d => ({ name: d.code, count: Math.floor(Math.random()*200+50) }))}>
              <XAxis dataKey="name" tick={{ fontSize:12 }} axisLine={false} tickLine={false} />
              <YAxis tick={{ fontSize:12 }} axisLine={false} tickLine={false} />
              <Tooltip contentStyle={{ borderRadius:8, border:'1px solid #E4E7F0', fontSize:12 }} />
              <Bar dataKey="count" fill="#4F6EF7" radius={[4,4,0,0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Departments table */}
      <div className="card">
        <h3 style={{ fontFamily:'var(--font-display)', fontSize:15, fontWeight:600, marginBottom:16 }}>
          Departments
        </h3>
        {depts.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">🏛️</div>
            <p>No departments yet. Add one in Courses.</p>
          </div>
        ) : (
          <div className="table-wrap">
            <table>
              <thead><tr>
                <th>Name</th><th>Code</th><th>Status</th>
              </tr></thead>
              <tbody>
                {depts.map(d => (
                  <tr key={d.id}>
                    <td style={{ fontWeight:500 }}>{d.name}</td>
                    <td><span className="badge badge-blue">{d.code}</span></td>
                    <td><span className={`badge ${d.isActive ? 'badge-green' : 'badge-gray'}`}>
                      {d.isActive ? 'Active' : 'Inactive'}
                    </span></td>
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
