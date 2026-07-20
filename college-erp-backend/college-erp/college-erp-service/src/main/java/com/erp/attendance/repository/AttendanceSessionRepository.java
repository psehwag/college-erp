package com.erp.attendance.repository;

import com.erp.attendance.entity.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {
    Optional<AttendanceSession> findBySessionToken(String token);
    List<AttendanceSession> findByFacultyIdAndStatus(Long facultyId, AttendanceSession.SessionStatus status);
}
