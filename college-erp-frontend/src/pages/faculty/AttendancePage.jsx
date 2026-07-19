import React, { useEffect, useState, useRef } from 'react';
import { attendanceAPI, courseAPI, faceAPI, studentAPI } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import toast from 'react-hot-toast';

const STATUS_OPTIONS = ['PRESENT', 'ABSENT', 'LATE', 'EXCUSED'];
const STATUS_COLOR   = { PRESENT:'badge-green', ABSENT:'badge-red', LATE:'badge-amber', EXCUSED:'badge-gray' };

export default function AttendancePage() {
  const { user } = useAuth();
  const [mode, setMode]         = useState('manual'); // 'manual' | 'face'
  const [batches, setBatches]   = useState([]);
  const [subjects, setSubjects] = useState([]);
  const [students, setStudents] = useState([]);
  const [selected, setSelected] = useState({ batchId:'', subjectId:'' });
  const [records, setRecords]   = useState({}); // { studentId: status }
  const [date, setDate]         = useState(new Date().toISOString().slice(0,10));
  const [saving, setSaving]     = useState(false);
  const [session, setSession]   = useState(null);
  const [scanning, setScanning] = useState(false);
  const webcamRef               = useRef(null);
  const intervalRef             = useRef(null);

  useEffect(() => {
    courseAPI.getDepartments()
      .then(r => r.data.data || [])
      .then(depts => Promise.all(depts.map(d => courseAPI.getBatchesByDept(d.id))))
      .then(results => setBatches(results.flatMap(r => r.data.data || [])))
      .catch(() => {});
  }, []);

  useEffect(() => {
    if (!selected.batchId) return;
    const batch = batches.find(b => b.id == selected.batchId);
    if (batch) {
      courseAPI.getSubjectsByCourse(batch.courseId, batch.currentSemester)
        .then(r => setSubjects(r.data.data || [])).catch(() => {});
      studentAPI.getByBatch(selected.batchId)
        .then(r => {
          const list = r.data.data || [];
          setStudents(list);
          const init = {};
          list.forEach(s => { init[s.id] = 'PRESENT'; });
          setRecords(init);
        }).catch(() => {});
    }
  }, [selected.batchId, batches]);

  // ── Manual submit ──────────────────────────────────────────────────────
  const submitManual = async () => {
    if (!selected.batchId || !selected.subjectId) return toast.error('Select batch and subject');
    setSaving(true);
    try {
      await attendanceAPI.markBulk({
        facultyId: user?.referenceId || 1,
        subjectId: +selected.subjectId,
        batchId: +selected.batchId,
        attendanceDate: date,
        studentAttendances: students.map(s => ({ studentId: s.id, status: records[s.id] || 'ABSENT' }))
      });
      toast.success(`Attendance marked for ${students.length} students`);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to mark attendance');
    } finally { setSaving(false); }
  };

  // ── Face recognition session ───────────────────────────────────────────
  const startFaceSession = async () => {
    if (!selected.batchId || !selected.subjectId) return toast.error('Select batch and subject');
    try {
      const res = await attendanceAPI.startSession({
        facultyId: user?.referenceId || 1,
        subjectId: +selected.subjectId,
        batchId: +selected.batchId
      });
      setSession(res.data.data);
      setScanning(true);
      toast.success('Session started — face recognition active');
    } catch { toast.error('Failed to start session'); }
  };

  const stopFaceSession = async () => {
    setScanning(false);
    clearInterval(intervalRef.current);
    if (session) {
      try { await attendanceAPI.endSession(session.id); toast.success('Session ended'); }
      catch {}
      setSession(null);
    }
  };

  // Capture frame from webcam and send to backend every 2s
  useEffect(() => {
    if (!scanning) return;
    intervalRef.current = setInterval(async () => {
      if (!webcamRef.current) return;
      try {
        const canvas = document.createElement('canvas');
        const video  = webcamRef.current;
        canvas.width  = video.videoWidth  || 320;
        canvas.height = video.videoHeight || 240;
        canvas.getContext('2d').drawImage(video, 0, 0);
        const b64 = canvas.toDataURL('image/jpeg').split(',')[1];

        const res = await faceAPI.recognize({
          batchId: +selected.batchId,
          subjectId: +selected.subjectId,
          facultyId: user?.referenceId || 1,
          sessionToken: session?.sessionToken,
          frameBase64: b64
        });
        const { recognized, studentId, confidenceScore } = res.data.data;
        if (recognized && studentId) {
          await attendanceAPI.markByFace({
            studentId, subjectId: +selected.subjectId,
            facultyId: user?.referenceId || 1,
            batchId: +selected.batchId,
            sessionToken: session?.sessionToken,
            confidenceScore
          });
          const s = students.find(st => st.id === studentId);
          toast.success(`✓ ${s?.fullName || 'Student'} marked present (${Math.round(confidenceScore)}%)`);
          setRecords(r => ({ ...r, [studentId]: 'PRESENT' }));
        }
      } catch {}
    }, 2000);
    return () => clearInterval(intervalRef.current);
  }, [scanning, session, selected, students, user]);

  const toggleStatus = (studentId) => {
    const order = STATUS_OPTIONS;
    const cur   = records[studentId] || 'PRESENT';
    const next  = order[(order.indexOf(cur) + 1) % order.length];
    setRecords(r => ({ ...r, [studentId]: next }));
  };

  const summary = Object.values(records).reduce((acc, v) => {
    acc[v] = (acc[v] || 0) + 1; return acc;
  }, {});

  return (
    <div>
      <div className="page-header">
        <div className="page-header-left">
          <h2>Mark attendance</h2>
          <p>Manual or AI face recognition</p>
        </div>
        <div className="tabs">
          <button className={`tab-btn ${mode==='manual'?'active':''}`} onClick={() => { setMode('manual'); stopFaceSession(); }}>Manual</button>
          <button className={`tab-btn ${mode==='face'?'active':''}`} onClick={() => setMode('face')}>🤖 Face Recognition</button>
        </div>
      </div>

      {/* Controls */}
      <div className="card mb-4">
        <div className="form-grid form-grid-3">
          <div className="form-group">
            <label className="form-label">Batch</label>
            <select className="form-select" value={selected.batchId}
              onChange={e => setSelected(s => ({ ...s, batchId: e.target.value }))}>
              <option value="">Select batch</option>
              {batches.map(b => <option key={b.id} value={b.id}>{b.name}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Subject</label>
            <select className="form-select" value={selected.subjectId}
              onChange={e => setSelected(s => ({ ...s, subjectId: e.target.value }))}>
              <option value="">Select subject</option>
              {subjects.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
            </select>
          </div>
          {mode === 'manual' && (
            <div className="form-group">
              <label className="form-label">Date</label>
              <input className="form-input" type="date" value={date} onChange={e => setDate(e.target.value)} />
            </div>
          )}
        </div>
      </div>

      {mode === 'face' ? (
        <div className="grid-2">
          {/* Webcam */}
          <div className="card">
            <h3 style={{ fontFamily:'var(--font-display)', fontSize:14, fontWeight:600, marginBottom:16 }}>Live camera</h3>
            <div className="webcam-frame" style={{ background:'#0F172A' }}>
              <video ref={webcamRef} autoPlay muted playsInline
                style={{ width:'100%', display:'block', borderRadius:8 }}
                onLoadedData={() => {}}
              />
              <div className="webcam-overlay">
                <div className="face-guide" />
              </div>
              {!scanning && (
                <div style={{ position:'absolute', inset:0, display:'flex', alignItems:'center',
                  justifyContent:'center', background:'rgba(0,0,0,0.6)', borderRadius:8 }}>
                  <p style={{ color:'rgba(255,255,255,0.6)', fontSize:13 }}>Session not started</p>
                </div>
              )}
            </div>
            <div style={{ marginTop:12, display:'flex', gap:8 }}>
              {!scanning
                ? <button className="btn btn-primary" onClick={startFaceSession}>▶ Start session</button>
                : <button className="btn btn-danger" onClick={stopFaceSession}>■ End session</button>
              }
              {session && <span className="badge badge-green" style={{ alignSelf:'center' }}>🔴 Live</span>}
            </div>
            <p style={{ fontSize:12, color:'var(--text-muted)', marginTop:8 }}>
              Camera captures a frame every 2 seconds. Recognised faces are automatically marked present.
            </p>
          </div>

          {/* Live log */}
          <div className="card">
            <h3 style={{ fontFamily:'var(--font-display)', fontSize:14, fontWeight:600, marginBottom:16 }}>
              Recognition log
            </h3>
            <div style={{ display:'flex', gap:12, marginBottom:16 }}>
              {Object.entries(summary).map(([k,v]) => (
                <div key={k} style={{ textAlign:'center' }}>
                  <div style={{ fontFamily:'var(--font-display)', fontSize:22, fontWeight:700 }}>{v}</div>
                  <div style={{ fontSize:11, color:'var(--text-muted)', textTransform:'uppercase' }}>{k}</div>
                </div>
              ))}
            </div>
            <div style={{ maxHeight:320, overflowY:'auto', display:'flex', flexDirection:'column', gap:8 }}>
              {students.filter(s => records[s.id] === 'PRESENT').map(s => (
                <div key={s.id} style={{ display:'flex', alignItems:'center', gap:10, padding:'8px 12px',
                  background:'var(--success-soft)', borderRadius:8 }}>
                  <span style={{ fontSize:18 }}>✓</span>
                  <div>
                    <div style={{ fontSize:13, fontWeight:500 }}>{s.fullName || s.enrollmentNumber}</div>
                    <div style={{ fontSize:11, color:'var(--text-muted)' }}>Marked present</div>
                  </div>
                </div>
              ))}
              {students.filter(s => records[s.id] === 'PRESENT').length === 0 && (
                <div className="empty-state" style={{ padding:24 }}>
                  <div className="empty-icon">📷</div>
                  <p>No faces recognised yet</p>
                </div>
              )}
            </div>
          </div>
        </div>
      ) : (
        <>
          {/* Summary bar */}
          {students.length > 0 && (
            <div style={{ display:'flex', gap:12, marginBottom:16 }}>
              {Object.entries(summary).map(([k,v]) => (
                <div key={k} className="card card-sm" style={{ display:'flex', alignItems:'center', gap:8 }}>
                  <span className={`badge ${STATUS_COLOR[k]}`}>{k}</span>
                  <span style={{ fontFamily:'var(--font-display)', fontWeight:700, fontSize:18 }}>{v}</span>
                </div>
              ))}
            </div>
          )}

          {/* Attendance grid */}
          {students.length === 0 ? (
            <div className="empty-state card">
              <div className="empty-icon">👥</div>
              <p>Select a batch to load students</p>
            </div>
          ) : (
            <>
              <div className="attendance-grid mb-4">
                {students.map(s => {
                  const status = records[s.id] || 'PRESENT';
                  return (
                    <div key={s.id}
                      className={`attendance-card ${status.toLowerCase()}`}
                      style={{ cursor:'pointer' }}
                      onClick={() => toggleStatus(s.id)}>
                      <div style={{ width:36, height:36, borderRadius:'50%', background:'var(--accent-soft)',
                        display:'flex', alignItems:'center', justifyContent:'center',
                        fontWeight:600, fontSize:13, color:'var(--accent)', flexShrink:0 }}>
                        {(s.firstName?.[0]||'').toUpperCase()}{(s.lastName?.[0]||'').toUpperCase()}
                      </div>
                      <div style={{ flex:1, minWidth:0 }}>
                        <div style={{ fontSize:13, fontWeight:500, overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap' }}>
                          {s.fullName || `${s.firstName} ${s.lastName}`}
                        </div>
                        <span className={`badge ${STATUS_COLOR[status]}`} style={{ fontSize:10.5 }}>{status}</span>
                      </div>
                    </div>
                  );
                })}
              </div>
              <button className="btn btn-primary" onClick={submitManual} disabled={saving}>
                {saving ? <><span className="spinner" /> Saving…</> : `✓ Submit attendance for ${students.length} students`}
              </button>
            </>
          )}
        </>
      )}
    </div>
  );
}
