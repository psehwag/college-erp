package com.erp.attendance.repository;

import com.erp.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByStudentId(Long studentId);
    List<Attendance> findBySubjectIdAndAttendanceDate(Long subjectId, LocalDate date);
    List<Attendance> findByBatchIdAndAttendanceDateBetween(Long batchId, LocalDate from, LocalDate to);

    Optional<Attendance> findByStudentIdAndSubjectIdAndAttendanceDate(
            Long studentId, Long subjectId, LocalDate date);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.studentId = :sid " +
           "AND a.subjectId = :subId AND a.status = 'PRESENT'")
    long countPresent(@Param("sid") Long studentId, @Param("subId") Long subjectId);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.studentId = :sid AND a.subjectId = :subId")
    long countTotal(@Param("sid") Long studentId, @Param("subId") Long subjectId);

    @Query("SELECT a.studentId FROM Attendance a WHERE a.subjectId = :subId " +
           "GROUP BY a.studentId " +
           "HAVING (COUNT(CASE WHEN a.status = 'PRESENT' THEN 1 END) * 100.0 / COUNT(a)) < :threshold")
    List<Long> findDefaulters(@Param("subId") Long subjectId, @Param("threshold") double threshold);

    @Query("SELECT a.studentId FROM Attendance a WHERE a.subjectId = :subId AND a.batchId = :batchId " +
           "GROUP BY a.studentId " +
           "HAVING (COUNT(CASE WHEN a.status = 'PRESENT' THEN 1 END) * 100.0 / COUNT(a)) < :threshold")
    List<Long> findDefaultersByBatch(@Param("subId") Long subjectId, @Param("batchId") Long batchId,
                                      @Param("threshold") double threshold);

    @Query("SELECT a FROM Attendance a WHERE a.studentId = :sid " +
           "AND a.attendanceDate BETWEEN :from AND :to ORDER BY a.attendanceDate DESC")
    List<Attendance> findByStudentAndRange(@Param("sid") Long studentId,
                                           @Param("from") LocalDate from,
                                           @Param("to") LocalDate to);

    // ── Dashboard stats ──────────────────────────────────────────────────

    long countByAttendanceDate(LocalDate date);
    long countByAttendanceDateAndStatus(LocalDate date, Attendance.AttendanceStatus status);
    List<Attendance> findByAttendanceDateBetween(LocalDate from, LocalDate to);

    // ── Cascade delete ───────────────────────────────────────────────────

    @Modifying
    void deleteByStudentId(Long studentId);

    @Modifying
    void deleteBySubjectId(Long subjectId);
}
