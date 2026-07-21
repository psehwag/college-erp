package com.erp.attendance.service;

import com.erp.attendance.dto.AttendanceDto;
import com.erp.attendance.entity.Attendance;
import com.erp.attendance.entity.AttendanceSession;
import com.erp.attendance.repository.AttendanceRepository;
import com.erp.attendance.repository.AttendanceSessionRepository;
import com.erp.common.exception.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@Transactional
public class AttendanceService {

    private static final Logger log = Logger.getLogger(AttendanceService.class.getName());

    private final AttendanceRepository attendanceRepo;
    private final AttendanceSessionRepository sessionRepo;

    public AttendanceService(AttendanceRepository attendanceRepo,
                             AttendanceSessionRepository sessionRepo) {
        this.attendanceRepo = attendanceRepo;
        this.sessionRepo    = sessionRepo;
    }

    // ── Bulk manual marking ───────────────────────────────────────────────

    public List<AttendanceDto.Response> markBulk(AttendanceDto.BulkMarkRequest req) {
        List<Attendance> toSave = new ArrayList<>();

        for (AttendanceDto.StudentMark sm : req.getStudentMarks()) {
            Optional<Attendance> existing = attendanceRepo
                    .findByStudentIdAndSubjectIdAndAttendanceDate(
                            sm.getStudentId(), req.getSubjectId(), req.getAttendanceDate());

            Attendance a;
            if (existing.isPresent()) {
                a = existing.get();
                a.setStatus(sm.getStatus());
                a.setRemarks(sm.getRemarks());
            } else {
                a = new Attendance();
                a.setStudentId(sm.getStudentId());
                a.setSubjectId(req.getSubjectId());
                a.setFacultyId(req.getFacultyId());
                a.setBatchId(req.getBatchId());
                a.setAttendanceDate(req.getAttendanceDate());
                a.setCheckInTime(LocalTime.now());
                a.setStatus(sm.getStatus());
                a.setMarkedBy(Attendance.MarkedBy.MANUAL);
                a.setRemarks(sm.getRemarks());
            }
            toSave.add(a);
        }

        List<Attendance> saved = attendanceRepo.saveAll(toSave);
        log.info("Bulk attendance marked for " + saved.size() + " students");
        return saved.stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Face recognition marking ──────────────────────────────────────────

    public AttendanceDto.Response markByFace(AttendanceDto.FaceMarkRequest req) {
        Optional<Attendance> existing = attendanceRepo
                .findByStudentIdAndSubjectIdAndAttendanceDate(
                        req.getStudentId(), req.getSubjectId(), LocalDate.now());

        if (existing.isPresent() &&
                existing.get().getStatus() == Attendance.AttendanceStatus.PRESENT) {
            return toResponse(existing.get());
        }

        Attendance a = new Attendance();
        a.setStudentId(req.getStudentId());
        a.setSubjectId(req.getSubjectId());
        a.setFacultyId(req.getFacultyId());
        a.setBatchId(req.getBatchId());
        a.setAttendanceDate(LocalDate.now());
        a.setCheckInTime(LocalTime.now());
        a.setStatus(Attendance.AttendanceStatus.PRESENT);
        a.setMarkedBy(Attendance.MarkedBy.FACE_RECOGNITION);
        a.setConfidenceScore(req.getConfidenceScore());
        a.setRemarks("Auto-marked via face recognition");
        return toResponse(attendanceRepo.save(a));
    }

    // ── Session management ────────────────────────────────────────────────

    public AttendanceDto.SessionResponse startSession(AttendanceDto.StartSessionRequest req) {
        AttendanceSession session = new AttendanceSession();
        session.setFacultyId(req.getFacultyId());
        session.setSubjectId(req.getSubjectId());
        session.setBatchId(req.getBatchId());
        session.setSessionDate(LocalDate.now());
        session.setStartTime(LocalTime.now());
        session.setStatus(AttendanceSession.SessionStatus.ACTIVE);
        session.setSessionToken(UUID.randomUUID().toString().replace("-", ""));
        return toSessionResponse(sessionRepo.save(session));
    }

    public AttendanceDto.SessionResponse endSession(Long sessionId) {
        AttendanceSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new AppException("Session not found", HttpStatus.NOT_FOUND));
        session.setStatus(AttendanceSession.SessionStatus.COMPLETED);
        session.setEndTime(LocalTime.now());
        return toSessionResponse(sessionRepo.save(session));
    }

    // ── Queries ───────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AttendanceDto.PercentageResponse getPercentage(Long studentId, Long subjectId) {
        long present = attendanceRepo.countPresent(studentId, subjectId);
        long total   = attendanceRepo.countTotal(studentId, subjectId);
        double pct   = total > 0 ? Math.round((present * 100.0 / total) * 100.0) / 100.0 : 0.0;

        AttendanceDto.PercentageResponse r = new AttendanceDto.PercentageResponse();
        r.setStudentId(studentId);
        r.setSubjectId(subjectId);
        r.setPresentClasses(present);
        r.setTotalClasses(total);
        r.setPercentage(pct);
        r.setShortfall(pct < 75.0);
        return r;
    }

    @Transactional(readOnly = true)
    public List<AttendanceDto.Response> getByDate(Long subjectId, LocalDate date) {
        return attendanceRepo.findBySubjectIdAndAttendanceDate(subjectId, date)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AttendanceDto.Response> getByStudent(Long studentId) {
        return attendanceRepo.findByStudentId(studentId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Long> getDefaulters(Long subjectId, Double threshold) {
        return attendanceRepo.findDefaulters(subjectId, threshold != null ? threshold : 75.0);
    }

    // ── Mappers ───────────────────────────────────────────────────────────

    private AttendanceDto.Response toResponse(Attendance a) {
        AttendanceDto.Response r = new AttendanceDto.Response();
        r.setId(a.getId());
        r.setStudentId(a.getStudentId());
        r.setSubjectId(a.getSubjectId());
        r.setFacultyId(a.getFacultyId());
        r.setBatchId(a.getBatchId());
        r.setAttendanceDate(a.getAttendanceDate());
        r.setCheckInTime(a.getCheckInTime());
        r.setStatus(a.getStatus());
        r.setMarkedBy(a.getMarkedBy());
        r.setConfidenceScore(a.getConfidenceScore());
        r.setRemarks(a.getRemarks());
        r.setCreatedAt(a.getCreatedAt());
        return r;
    }

    private AttendanceDto.SessionResponse toSessionResponse(AttendanceSession s) {
        AttendanceDto.SessionResponse r = new AttendanceDto.SessionResponse();
        r.setId(s.getId());
        r.setFacultyId(s.getFacultyId());
        r.setSubjectId(s.getSubjectId());
        r.setBatchId(s.getBatchId());
        r.setSessionDate(s.getSessionDate());
        r.setStartTime(s.getStartTime());
        r.setEndTime(s.getEndTime());
        r.setStatus(s.getStatus());
        r.setSessionToken(s.getSessionToken());
        return r;
    }
}
