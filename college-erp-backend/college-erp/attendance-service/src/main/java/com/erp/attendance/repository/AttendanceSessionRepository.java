package com.erp.attendance.repository;

import com.erp.attendance.entity.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {
    Optional<AttendanceSession> findBySessionToken(String sessionToken);
    List<AttendanceSession> findByFacultyIdAndSessionDate(Long facultyId, LocalDate date);
    List<AttendanceSession> findBySubjectIdAndSessionDateAndStatus(
            Long subjectId, LocalDate date, AttendanceSession.SessionStatus status);
    List<AttendanceSession> findByBatchIdAndSessionDateBetween(Long batchId, LocalDate from, LocalDate to);
}
