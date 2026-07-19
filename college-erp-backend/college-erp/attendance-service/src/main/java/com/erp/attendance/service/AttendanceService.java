package com.erp.attendance.service;

import com.erp.attendance.dto.AttendanceDto;
import com.erp.attendance.entity.Attendance;
import com.erp.attendance.entity.AttendanceSession;
import com.erp.attendance.exception.AttendanceException;
import com.erp.attendance.repository.AttendanceRepository;
import com.erp.attendance.repository.AttendanceSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceSessionRepository sessionRepository;

    // Manual attendance marking by faculty
    public List<AttendanceDto.Response> markAttendanceBulk(AttendanceDto.BulkMarkRequest request) {
        List<Attendance> attendanceList = new ArrayList<>();

        for (AttendanceDto.StudentAttendance sa : request.getStudentAttendances()) {
            Optional<Attendance> existing = attendanceRepository
                    .findByStudentIdAndSubjectIdAndAttendanceDate(
                            sa.getStudentId(), request.getSubjectId(), request.getAttendanceDate());

            Attendance attendance;
            if (existing.isPresent()) {
                attendance = existing.get();
                attendance.setStatus(sa.getStatus());
                attendance.setRemarks(sa.getRemarks());
            } else {
                attendance = Attendance.builder()
                        .studentId(sa.getStudentId())
                        .subjectId(request.getSubjectId())
                        .facultyId(request.getFacultyId())
                        .batchId(request.getBatchId())
                        .attendanceDate(request.getAttendanceDate())
                        .checkInTime(LocalTime.now())
                        .status(sa.getStatus())
                        .markedBy(Attendance.MarkedBy.MANUAL)
                        .remarks(sa.getRemarks())
                        .build();
            }
            attendanceList.add(attendance);
        }

        List<Attendance> saved = attendanceRepository.saveAll(attendanceList);
        log.info("Bulk attendance marked for {} students, subject: {}", saved.size(), request.getSubjectId());
        return saved.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // AI face recognition attendance marking
    public AttendanceDto.Response markByFaceRecognition(AttendanceDto.FaceRecognitionMarkRequest request) {
        Optional<Attendance> existing = attendanceRepository
                .findByStudentIdAndSubjectIdAndAttendanceDate(
                        request.getStudentId(), request.getSubjectId(), LocalDate.now());

        if (existing.isPresent() && existing.get().getStatus() == Attendance.AttendanceStatus.PRESENT) {
            log.info("Attendance already marked for student: {}", request.getStudentId());
            return mapToResponse(existing.get());
        }

        Attendance attendance = Attendance.builder()
                .studentId(request.getStudentId())
                .subjectId(request.getSubjectId())
                .facultyId(request.getFacultyId())
                .batchId(request.getBatchId())
                .attendanceDate(LocalDate.now())
                .checkInTime(LocalTime.now())
                .status(Attendance.AttendanceStatus.PRESENT)
                .markedBy(Attendance.MarkedBy.FACE_RECOGNITION)
                .confidenceScore(request.getConfidenceScore())
                .remarks("Auto-marked via face recognition")
                .build();

        attendance = attendanceRepository.save(attendance);
        log.info("Face recognition attendance marked: student={}, confidence={}", 
                request.getStudentId(), request.getConfidenceScore());
        return mapToResponse(attendance);
    }

    // Start attendance session for face recognition
    public AttendanceDto.SessionResponse startSession(AttendanceDto.StartSessionRequest request) {
        AttendanceSession session = AttendanceSession.builder()
                .facultyId(request.getFacultyId())
                .subjectId(request.getSubjectId())
                .batchId(request.getBatchId())
                .sessionDate(LocalDate.now())
                .startTime(LocalTime.now())
                .status(AttendanceSession.SessionStatus.ACTIVE)
                .sessionToken(UUID.randomUUID().toString().replace("-", ""))
                .build();

        session = sessionRepository.save(session);
        log.info("Attendance session started: id={}", session.getId());
        return mapToSessionResponse(session);
    }

    public AttendanceDto.SessionResponse endSession(Long sessionId) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new AttendanceException("Session not found"));
        session.setStatus(AttendanceSession.SessionStatus.COMPLETED);
        session.setEndTime(LocalTime.now());
        return mapToSessionResponse(sessionRepository.save(session));
    }

    @Transactional(readOnly = true)
    public AttendanceDto.AttendancePercentage getStudentAttendance(Long studentId, Long subjectId) {
        Long present = attendanceRepository.countPresent(studentId, subjectId);
        Long total = attendanceRepository.countTotal(studentId, subjectId);
        double percentage = total > 0 ? (present * 100.0 / total) : 0;

        return AttendanceDto.AttendancePercentage.builder()
                .studentId(studentId).subjectId(subjectId)
                .totalClasses(total).presentClasses(present)
                .percentage(Math.round(percentage * 100.0) / 100.0)
                .isShortfall(percentage < 75.0)
                .build();
    }

    @Transactional(readOnly = true)
    public List<AttendanceDto.Response> getAttendanceByDate(Long subjectId, LocalDate date) {
        return attendanceRepository.findBySubjectIdAndAttendanceDate(subjectId, date)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Long> getDefaulters(Long subjectId, Double threshold) {
        return attendanceRepository.findDefaulters(subjectId, threshold != null ? threshold : 75.0);
    }

    private AttendanceDto.Response mapToResponse(Attendance a) {
        return AttendanceDto.Response.builder()
                .id(a.getId()).studentId(a.getStudentId())
                .subjectId(a.getSubjectId()).facultyId(a.getFacultyId())
                .attendanceDate(a.getAttendanceDate()).checkInTime(a.getCheckInTime())
                .status(a.getStatus()).markedBy(a.getMarkedBy())
                .confidenceScore(a.getConfidenceScore()).remarks(a.getRemarks())
                .build();
    }

    private AttendanceDto.SessionResponse mapToSessionResponse(AttendanceSession s) {
        return AttendanceDto.SessionResponse.builder()
                .id(s.getId()).facultyId(s.getFacultyId())
                .subjectId(s.getSubjectId()).batchId(s.getBatchId())
                .sessionDate(s.getSessionDate()).startTime(s.getStartTime())
                .endTime(s.getEndTime()).status(s.getStatus())
                .sessionToken(s.getSessionToken()).build();
    }
}
