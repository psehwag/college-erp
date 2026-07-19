package com.erp.attendance.repository;

import com.erp.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByStudentIdAndSubjectId(Long studentId, Long subjectId);
    List<Attendance> findByStudentId(Long studentId);
    List<Attendance> findBySubjectIdAndAttendanceDate(Long subjectId, LocalDate date);
    List<Attendance> findByBatchIdAndAttendanceDateBetween(Long batchId, LocalDate from, LocalDate to);

    Optional<Attendance> findByStudentIdAndSubjectIdAndAttendanceDate(
            Long studentId, Long subjectId, LocalDate date);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.studentId = :studentId " +
           "AND a.subjectId = :subjectId AND a.status = 'PRESENT'")
    Long countPresent(@Param("studentId") Long studentId, @Param("subjectId") Long subjectId);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.studentId = :studentId AND a.subjectId = :subjectId")
    Long countTotal(@Param("studentId") Long studentId, @Param("subjectId") Long subjectId);

    @Query("SELECT a.studentId, COUNT(CASE WHEN a.status = 'PRESENT' THEN 1 END) as present, " +
           "COUNT(a) as total FROM Attendance a WHERE a.subjectId = :subjectId " +
           "GROUP BY a.studentId")
    List<Object[]> getAttendanceSummaryBySubject(@Param("subjectId") Long subjectId);

    @Query("SELECT a FROM Attendance a WHERE a.studentId = :studentId " +
           "AND a.attendanceDate BETWEEN :from AND :to ORDER BY a.attendanceDate DESC")
    List<Attendance> findByStudentAndDateRange(
            @Param("studentId") Long studentId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("SELECT a.studentId FROM Attendance a WHERE a.subjectId = :subjectId " +
           "GROUP BY a.studentId " +
           "HAVING (COUNT(CASE WHEN a.status = 'PRESENT' THEN 1 END) * 100.0 / COUNT(a)) < :threshold")
    List<Long> findDefaulters(@Param("subjectId") Long subjectId, @Param("threshold") Double threshold);
}
