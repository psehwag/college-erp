import React, { useEffect, useState, useRef } from 'react';
import { attendanceAPI, faceAPI, studentAPI, facultyAPI } from '../../services/api';
import AcademicCascadeSelect from '../../components/common/AcademicCascadeSelect';
import { useAuth } from '../../context/AuthContext';
import toast from 'react-hot-toast';

const STATUS_OPTIONS = ['PRESENT', 'ABSENT', 'LATE', 'EXCUSED'];
const STATUS_COLOR   = { PRESENT:'badge-green', ABSENT:'badge-red', LATE:'badge-amber', EXCUSED:'badge-gray' };

export default function AttendancePage() {
  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMIN';
  const [mode, setMode]         = useState('manual'); // 'manual' | 'face'
  const [sel, setSel]           = useState({});        // dept/course/semester/subject/batch
  const [students, setStudents] = useState([]);
  const [records, setRecords]   = useState({});         // { studentId: status }
  const [selectedIds, setSelectedIds] = useState(new Set()); // multiselect checkboxes
  const [date, setDate]         = useState(new Date().toISOString().slice(0,10));
  const [saving, setSaving]     = useState(false);
  const [session, setSession]   = useState(null);
  const [scanning, setScanning] = useState(false);
  const webcamRef               = useRef(null);
  const intervalRef             = useRef(null);
  const streamRef                = useRef(null);

  // Face mode's own recognition log — kept separate from `records` (which
  // defaults every student to PRESENT the moment a batch loads) so it only
  // ever reflects genuine recognition events, not that stale default.
  const [recognizedIds, setRecognizedIds] = useState(new Set());
  const [lastScanStatus, setLastScanStatus] = useState('');

  // ADMIN isn't a faculty member, so there's no natural facultyId to
  // attribute the marking to — let admin pick one explicitly.
  const [facultyOptions, setFacultyOptions] = useState([]);
  const [selectedFacultyId, setSelectedFacultyId] = useState('');
  const facultyId = isAdmin ? (selectedFacultyId ? +selectedFacultyId : null) : (user?.referenceId || null);

  useEffect(() => {
    if (!isAdmin || !sel.departmentId) { setFacultyOptions([]); return; }
    setSelectedFacultyId('');
    facultyAPI.getByDept(sel.departmentId)
      .then(r => setFacultyOptions(r.data.data || []))
      .catch(() => setFacultyOptions([]));
  }, [isAdmin, sel.departmentId]);

  // Load students whenever the batch changes
  useEffect(() => {
    if (!sel.batchId) { setStudents([]); return; }
    studentAPI.getByBatch(sel.batchId)
      .then(r => {
        const list = r.data.data || [];
        setStudents(list);
        const init = {};
        list.forEach(s => { init[s.id] = 'PRESENT'; });
        setRecords(init);
        // Everyone starts checked — submission includes the whole roster
        // by default, and unchecking someone excludes just them.
        setSelectedIds(new Set(list.map(s => s.id)));
        setRecognizedIds(new Set());
        setLastScanStatus('');
      }).catch(() => {});
  }, [sel.batchId]);

  const isReady = sel.departmentId && sel.courseId && sel.semester && sel.subjectId && sel.batchId
    && (!isAdmin || selectedFacultyId);

  // ── Manual submit ──────────────────────────────────────────────────────
  const submitManual = async () => {
    if (!isReady) return toast.error(isAdmin
      ? 'Select department, course, semester, subject, batch and faculty'
      : 'Select department, course, semester, subject and batch');
    if (selectedIds.size === 0) return toast.error('Check at least one student to submit');
    setSaving(true);
    try {
      await attendanceAPI.markBulk({
        facultyId,
        subjectId: +sel.subjectId,
        batchId: +sel.batchId,
        attendanceDate: date,
        studentMarks: students
          .filter(s => selectedIds.has(s.id))
          .map(s => ({ studentId: s.id, status: records[s.id] || 'ABSENT' }))
      });
      toast.success(`Attendance marked for ${selectedIds.size} students`);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to mark attendance');
    } finally { setSaving(false); }
  };

  // ── Multiselect bulk actions ──────────────────────────────────────────
  const toggleSelect = (id) => {
    setSelectedIds(prev => {
      const next = new Set(prev);
      next.has(id) ? next.delete(id) : next.add(id);
      return next;
    });
  };
  const selectAll = () => setSelectedIds(new Set(students.map(s => s.id)));
  const clearSelection = () => setSelectedIds(new Set());
  const bulkSetStatus = (status) => {
    if (selectedIds.size === 0) return toast.error('Select at least one student first');
    setRecords(r => {
      const next = { ...r };
      selectedIds.forEach(id => { next[id] = status; });
      return next;
    });
    toast.success(`Marked ${selectedIds.size} student(s) as ${status}`);
  };

  const toggleStatus = (studentId) => {
    const order = STATUS_OPTIONS;
    const cur   = records[studentId] || 'PRESENT';
    const next  = order[(order.indexOf(cur) + 1) % order.length];
    setRecords(r => ({ ...r, [studentId]: next }));
  };

  // ── Face recognition session ───────────────────────────────────────────
  const startFaceSession = async () => {
    if (!isReady) return toast.error(isAdmin
      ? 'Select department, course, semester, subject, batch and faculty'
      : 'Select department, course, semester, subject and batch');
    try {
      const res = await attendanceAPI.startSession({
        facultyId,
        subjectId: +sel.subjectId,
        batchId: +sel.batchId
      });
      setSession(res.data.data);
      setRecognizedIds(new Set());
      setLastScanStatus('');

      const stream = await navigator.mediaDevices.getUserMedia({ video: true });
      streamRef.current = stream;
      if (webcamRef.current) webcamRef.current.srcObject = stream;

      setScanning(true);
      toast.success('Session started — show faces to the camera');
    } catch (err) {
      toast.error(err.response?.data?.message || err.message || 'Failed to start session');
    }
  };

  const stopFaceSession = async () => {
    setScanning(false);
    clearInterval(intervalRef.current);
    if (streamRef.current) {
      streamRef.current.getTracks().forEach(t => t.stop());
      streamRef.current = null;
    }
    if (session) {
      try { await attendanceAPI.endSession(session.id); toast.success('Session ended'); }
      catch {}
      setSession(null);
    }
  };

  useEffect(() => {
    if (!scanning) return;
    intervalRef.current = setInterval(async () => {
      if (!webcamRef.current || !webcamRef.current.videoWidth) {
        setLastScanStatus('Waiting for camera…');
        return;
      }
      try {
        const canvas = document.createElement('canvas');
        const video  = webcamRef.current;
        canvas.width  = video.videoWidth  || 320;
        canvas.height = video.videoHeight || 240;
        canvas.getContext('2d').drawImage(video, 0, 0);
        const b64 = canvas.toDataURL('image/jpeg').split(',')[1];

        const res = await faceAPI.recognize({
          batchId: +sel.batchId,
          subjectId: +sel.subjectId,
          facultyId,
          sessionToken: session?.sessionToken,
          frameBase64: b64
        });
        const { recognized, studentId, confidenceScore, message } = res.data.data;

        if (recognized && studentId) {
          setLastScanStatus(`Recognised (${Math.round(confidenceScore)}%)`);
          // Already marked this session — skip the redundant API call.
          if (!recognizedIds.has(studentId)) {
            await attendanceAPI.markByFace({
              studentId, subjectId: +sel.subjectId,
              facultyId,
              batchId: +sel.batchId,
              sessionToken: session?.sessionToken,
              confidenceScore
            });
            const s = students.find(st => st.id === studentId);
            toast.success(`✓ ${s?.fullName || 'Student'} marked present (${Math.round(confidenceScore)}%)`);
            setRecognizedIds(prev => new Set(prev).add(studentId));
            setRecords(r => ({ ...r, [studentId]: 'PRESENT' }));
          }
        } else {
          setLastScanStatus(message || 'No face recognised in this frame');
        }
      } catch (err) {
        console.error('Face recognition tick failed:', err);
        setLastScanStatus(err.response?.data?.message || 'Recognition request failed — see browser console');
      }
    }, 2000);
    return () => clearInterval(intervalRef.current);
  }, [scanning, session, sel, students, facultyId, recognizedIds]);

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

      {/* Cascading selector */}
      <div className="card mb-4">
        <AcademicCascadeSelect value={sel} onChange={setSel} />
        {isAdmin && (
          <div className="form-group" style={{ marginTop:12, maxWidth:280 }}>
            <label className="form-label">Faculty (attribute this marking to)</label>
            <select className="form-select" value={selectedFacultyId} disabled={!sel.departmentId}
              onChange={e => setSelectedFacultyId(e.target.value)}>
              <option value="">Select faculty</option>
              {facultyOptions.map(f => <option key={f.id} value={f.id}>{f.fullName}</option>)}
            </select>
          </div>
        )}
        {mode === 'manual' && (
          <div className="form-group" style={{ marginTop:12, maxWidth:220 }}>
            <label className="form-label">Date</label>
            <input className="form-input" type="date" value={date} onChange={e => setDate(e.target.value)} />
          </div>
        )}
      </div>

      {!isReady ? (
        <div className="empty-state card">
          <div className="empty-icon">📋</div>
          <p>Select department, course, semester, subject and batch to continue</p>
        </div>
      ) : mode === 'face' ? (
        <div className="grid-2">
          <div className="card">
            <h3 style={{ fontFamily:'var(--font-display)', fontSize:14, fontWeight:600, marginBottom:16 }}>Live camera</h3>
            <div className="webcam-frame" style={{ background:'#0F172A' }}>
              <video ref={webcamRef} autoPlay muted playsInline
                style={{ width:'100%', display:'block', borderRadius:8 }} />
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
              Camera captures a frame every 2 seconds. Recognised faces from this batch's trained
              model are automatically marked present.
            </p>
            {scanning && (
              <p style={{ fontSize:12, color:'var(--text-muted)', marginTop:8, fontStyle:'italic' }}>
                Last scan: {lastScanStatus || '…'}
              </p>
            )}
          </div>

          <div className="card">
            <h3 style={{ fontFamily:'var(--font-display)', fontSize:14, fontWeight:600, marginBottom:16 }}>
              Recognition log
            </h3>
            <div style={{ display:'flex', gap:12, marginBottom:16 }}>
              <div style={{ textAlign:'center' }}>
                <div style={{ fontFamily:'var(--font-display)', fontSize:22, fontWeight:700 }}>{recognizedIds.size}</div>
                <div style={{ fontSize:11, color:'var(--text-muted)', textTransform:'uppercase' }}>Recognised</div>
              </div>
              <div style={{ textAlign:'center' }}>
                <div style={{ fontFamily:'var(--font-display)', fontSize:22, fontWeight:700 }}>{students.length - recognizedIds.size}</div>
                <div style={{ fontSize:11, color:'var(--text-muted)', textTransform:'uppercase' }}>Remaining</div>
              </div>
            </div>
            <div style={{ maxHeight:320, overflowY:'auto', display:'flex', flexDirection:'column', gap:8 }}>
              {students.filter(s => recognizedIds.has(s.id)).map(s => (
                <div key={s.id} style={{ display:'flex', alignItems:'center', gap:10, padding:'8px 12px',
                  background:'var(--success-soft)', borderRadius:8 }}>
                  <span style={{ fontSize:18 }}>✓</span>
                  <div>
                    <div style={{ fontSize:13, fontWeight:500 }}>{s.fullName || s.enrollmentNumber}</div>
                    <div style={{ fontSize:11, color:'var(--text-muted)' }}>Marked present</div>
                  </div>
                </div>
              ))}
              {recognizedIds.size === 0 && (
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
          {students.length > 0 && (
            <div style={{ display:'flex', gap:12, marginBottom:16, flexWrap:'wrap', alignItems:'center' }}>
              {Object.entries(summary).map(([k,v]) => (
                <div key={k} className="card card-sm" style={{ display:'flex', alignItems:'center', gap:8 }}>
                  <span className={`badge ${STATUS_COLOR[k]}`}>{k}</span>
                  <span style={{ fontFamily:'var(--font-display)', fontWeight:700, fontSize:18 }}>{v}</span>
                </div>
              ))}
              <div style={{ flex:1 }} />
              <span className="text-sm text-muted">{selectedIds.size} selected</span>
              <button className="btn btn-secondary btn-sm" onClick={selectAll}>Select all</button>
              <button className="btn btn-secondary btn-sm" onClick={clearSelection}>Clear</button>
              <button className="btn btn-primary btn-sm" onClick={() => bulkSetStatus('PRESENT')}>Mark Present</button>
              <button className="btn btn-danger btn-sm" onClick={() => bulkSetStatus('ABSENT')}>Mark Absent</button>
            </div>
          )}

          {students.length === 0 ? (
            <div className="empty-state card">
              <div className="empty-icon">👥</div>
              <p>No students found for this batch</p>
            </div>
          ) : (
            <>
              <div className="attendance-grid mb-4">
                {students.map(s => {
                  const status = records[s.id] || 'PRESENT';
                  const checked = selectedIds.has(s.id);
                  return (
                    <div key={s.id}
                      className={`attendance-card ${status.toLowerCase()}`}
                      style={{ cursor:'pointer', position:'relative' }}
                      onClick={() => toggleStatus(s.id)}>
                      <input type="checkbox" checked={checked}
                        onClick={e => e.stopPropagation()}
                        onChange={() => toggleSelect(s.id)}
                        style={{ position:'absolute', top:8, right:8, width:16, height:16 }} />
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
              <button className="btn btn-primary" onClick={submitManual} disabled={saving || selectedIds.size === 0}>
                {saving ? <><span className="spinner" /> Saving…</> : `✓ Submit attendance for ${selectedIds.size} students`}
              </button>
            </>
          )}
        </>
      )}
    </div>
  );
}
