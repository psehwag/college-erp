import React, { useState, useRef, useEffect } from 'react';
import { faceAPI, studentAPI } from '../../services/api';
import AcademicCascadeSelect from '../../components/common/AcademicCascadeSelect';
import toast from 'react-hot-toast';

export default function FaceEnrollPage() {
  const [sel, setSel]             = useState({});
  const [students, setStudents]   = useState([]);
  const [selStudent, setSelStudent] = useState(null);
  const [photos, setPhotos]       = useState([]);
  const [enrolling, setEnrolling] = useState(false);
  const [training, setTraining]   = useState(false);
  const fileRef                   = useRef();

  const isReady = sel.departmentId && sel.courseId && sel.semester && sel.subjectId && sel.batchId;

  useEffect(() => {
    if (!sel.batchId) { setStudents([]); return; }
    studentAPI.getByBatch(sel.batchId).then(r => setStudents(r.data.data || [])).catch(() => {});
  }, [sel.batchId]);

  const onFiles = e => {
    const files = Array.from(e.target.files).slice(0, 10);
    setPhotos(files);
  };

  const enroll = async () => {
    if (!selStudent) return toast.error('Select a student');
    if (photos.length === 0) return toast.error('Upload at least 1 photo');
    setEnrolling(true);
    try {
      const fd = new FormData();
      photos.forEach(f => fd.append('photos', f));
      const res = await faceAPI.enroll(selStudent.id, fd);
      toast.success(`Enrolled ${res.data.data.enrolledImages} face image(s) for ${selStudent.fullName}`);
      setPhotos([]); setSelStudent(null);
      studentAPI.getByBatch(sel.batchId).then(r => setStudents(r.data.data || []));
    } catch (err) {
      toast.error(err.response?.data?.message || 'Enrollment failed — no usable face detected in the photos');
    } finally { setEnrolling(false); }
  };

  const trainModel = async () => {
    if (!sel.batchId) return toast.error('Select a batch first');
    setTraining(true);
    try {
      const res = await faceAPI.train({ batchId: +sel.batchId, studentIds: students.map(s => s.id) });
      toast.success(res.data.data.message || `Model trained with ${res.data.data.trainedSamples} samples`);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Training failed');
    } finally { setTraining(false); }
  };

  return (
    <div>
      <div className="page-header">
        <div className="page-header-left">
          <h2>Face enrollment</h2>
          <p>Enroll student faces and train the recognition model</p>
        </div>
      </div>

      <div className="card mb-6" style={{ background:'var(--accent-soft)', border:'1px solid rgba(79,110,247,0.15)' }}>
        <h3 style={{ fontFamily:'var(--font-display)', fontSize:14, fontWeight:600, color:'var(--accent)', marginBottom:10 }}>
          🤖 How face recognition works
        </h3>
        <div style={{ display:'flex', gap:24, flexWrap:'wrap' }}>
          {[
            ['1','Select batch','Choose department → course → semester → subject → batch'],
            ['2','Upload photos','Upload 5–10 clear face photos with varied angles'],
            ['3','Enroll','Faces are detected and stored for that student'],
            ['4','Train model','Train the model for the batch after enrolling all students'],
            ['5','Take attendance','Faculty starts a live session and the camera recognises students'],
          ].map(([n,t,d]) => (
            <div key={n} style={{ display:'flex', gap:10, alignItems:'flex-start', minWidth:160 }}>
              <div style={{ width:24, height:24, borderRadius:'50%', background:'var(--accent)',
                color:'#fff', display:'flex', alignItems:'center', justifyContent:'center',
                fontSize:11, fontWeight:700, flexShrink:0 }}>{n}</div>
              <div>
                <div style={{ fontSize:13, fontWeight:600 }}>{t}</div>
                <div style={{ fontSize:12, color:'var(--text-muted)' }}>{d}</div>
              </div>
            </div>
          ))}
        </div>
      </div>

      <div className="card mb-4">
        <AcademicCascadeSelect value={sel} onChange={v => { setSel(v); setSelStudent(null); }} />
      </div>

      {!isReady ? (
        <div className="empty-state card">
          <div className="empty-icon">📋</div>
          <p>Select department, course, semester, subject and batch to continue</p>
        </div>
      ) : (
        <div className="grid-2">
          <div className="card">
            <h3 style={{ fontFamily:'var(--font-display)', fontSize:15, fontWeight:600, marginBottom:16 }}>
              Enroll student
            </h3>

            {students.length === 0 ? (
              <div className="empty-state" style={{ padding:20 }}><div className="empty-icon">👥</div><p>No students in this batch</p></div>
            ) : (
              <div className="form-group mb-4">
                <label className="form-label">Student</label>
                <div style={{ maxHeight:200, overflowY:'auto', border:'1px solid var(--border)', borderRadius:8 }}>
                  {students.map(s => (
                    <div key={s.id}
                      onClick={() => setSelStudent(s)}
                      style={{ padding:'9px 14px', cursor:'pointer', fontSize:13,
                        background: selStudent?.id === s.id ? 'var(--accent-soft)' : 'transparent',
                        borderBottom:'1px solid var(--border)' }}>
                      <div style={{ fontWeight:500 }}>{s.fullName || s.enrollmentNumber}</div>
                      <div style={{ fontSize:11, color:'var(--text-muted)', display:'flex', gap:8 }}>
                        <span>{s.enrollmentNumber}</span>
                        {s.faceEnrolled
                          ? <span style={{ color:'var(--success)' }}>✓ Enrolled</span>
                          : <span style={{ color:'var(--amber)' }}>Not enrolled</span>}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {selStudent && (
              <>
                <div style={{ padding:'12px 14px', background:'var(--bg)', borderRadius:8, marginBottom:16 }}>
                  <div style={{ fontWeight:600, fontSize:13 }}>Selected: {selStudent.fullName}</div>
                  <div style={{ fontSize:12, color:'var(--text-muted)' }}>{selStudent.enrollmentNumber}</div>
                </div>

                <div className="form-group mb-4">
                  <label className="form-label">Upload photos (5–10 recommended)</label>
                  <div style={{ border:'2px dashed var(--border)', borderRadius:8, padding:20,
                    textAlign:'center', cursor:'pointer' }} onClick={() => fileRef.current?.click()}>
                    {photos.length > 0
                      ? <p style={{ fontSize:13, color:'var(--accent)', fontWeight:500 }}>
                          {photos.length} photo{photos.length!==1?'s':''} selected
                        </p>
                      : <>
                          <div style={{ fontSize:32, marginBottom:8 }}>📸</div>
                          <p style={{ fontSize:13, color:'var(--text-muted)' }}>Click to select photos</p>
                          <p style={{ fontSize:11, color:'var(--text-muted)' }}>JPG, PNG · max 10 files</p>
                        </>}
                  </div>
                  <input ref={fileRef} type="file" accept="image/*" multiple style={{ display:'none' }} onChange={onFiles} />
                </div>

                <button className="btn btn-primary" onClick={enroll} disabled={enrolling} style={{ width:'100%', justifyContent:'center' }}>
                  {enrolling ? <><span className="spinner"/>Enrolling…</> : '✓ Enroll face'}
                </button>
              </>
            )}
          </div>

          <div className="card">
            <h3 style={{ fontFamily:'var(--font-display)', fontSize:15, fontWeight:600, marginBottom:16 }}>
              Train recognition model
            </h3>
            <div style={{ padding:20, background:'var(--bg)', borderRadius:8, marginBottom:20 }}>
              <div style={{ fontSize:13, color:'var(--text-muted)', marginBottom:4 }}>Students in batch: {students.length}</div>
              <div style={{ fontSize:13, color:'var(--text-muted)' }}>
                Enrolled: {students.filter(s => s.faceEnrolled).length}
              </div>
            </div>
            <div style={{ padding:14, background:'var(--amber-soft)', borderRadius:8, marginBottom:20 }}>
              <p style={{ fontSize:12.5, color:'#92400e' }}>
                ⚠ Train the model after enrolling all students in the batch. Re-train whenever
                new students are enrolled. Training may take a few seconds.
              </p>
            </div>
            <button className="btn btn-primary" onClick={trainModel} disabled={training || students.length===0}
              style={{ width:'100%', justifyContent:'center' }}>
              {training ? <><span className="spinner"/>Training model…</> : '🧠 Train model for this batch'}
            </button>

            {students.length > 0 && (
              <div style={{ marginTop:20 }}>
                <p style={{ fontSize:12.5, fontWeight:600, color:'var(--text-muted)', marginBottom:10, textTransform:'uppercase', letterSpacing:'0.5px' }}>
                  Enrollment status
                </p>
                <div style={{ display:'flex', flexDirection:'column', gap:6 }}>
                  {students.map(s => (
                    <div key={s.id} style={{ display:'flex', justifyContent:'space-between', alignItems:'center',
                      padding:'7px 12px', background:'var(--bg)', borderRadius:6, fontSize:13 }}>
                      <span>{s.fullName || s.enrollmentNumber}</span>
                      <span className={`badge ${s.faceEnrolled?'badge-green':'badge-gray'}`}>
                        {s.faceEnrolled ? '✓ Done' : 'Pending'}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
