package com.erp.marks.repository;

import com.erp.marks.entity.Marks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MarksRepository extends JpaRepository<Marks, Long> {

    List<Marks> findByStudentId(Long studentId);
    List<Marks> findByStudentIdAndSemester(Long studentId, Integer semester);
    List<Marks> findByStudentIdAndSubjectId(Long studentId, Long subjectId);
    List<Marks> findBySubjectIdAndExamTypeAndAcademicYear(Long subjectId, Marks.ExamType examType, String year);
    List<Marks> findByBatchIdAndSubjectIdAndExamType(Long batchId, Long subjectId, Marks.ExamType examType);

    Optional<Marks> findByStudentIdAndSubjectIdAndExamTypeAndAcademicYear(
            Long studentId, Long subjectId, Marks.ExamType examType, String academicYear);

    @Query("SELECT AVG(m.marksObtained / m.maxMarks * 100) FROM Marks m " +
           "WHERE m.studentId = :studentId AND m.semester = :semester")
    Double getAveragePercentageBySemester(@Param("studentId") Long studentId, @Param("semester") Integer semester);

    @Query("SELECT m FROM Marks m WHERE m.batchId = :batchId AND m.semester = :semester " +
           "ORDER BY m.studentId, m.subjectId")
    List<Marks> findByBatchAndSemester(@Param("batchId") Long batchId, @Param("semester") Integer semester);

    @Query("SELECT m.studentId, SUM(m.marksObtained), SUM(m.maxMarks) FROM Marks m " +
           "WHERE m.batchId = :batchId AND m.semester = :semester AND m.examType = :examType " +
           "GROUP BY m.studentId")
    List<Object[]> getAggregatedMarksByBatch(
            @Param("batchId") Long batchId,
            @Param("semester") Integer semester,
            @Param("examType") Marks.ExamType examType);
}
