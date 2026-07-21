package com.erp.marks.repository;

import com.erp.marks.entity.Marks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarksRepository extends JpaRepository<Marks, Long> {

    List<Marks> findByStudentId(Long studentId);
    List<Marks> findByStudentIdAndSemester(Long studentId, Integer semester);
    List<Marks> findByBatchIdAndSubjectIdAndExamType(Long batchId, Long subjectId, Marks.ExamType examType);
    List<Marks> findByBatchIdAndSubjectId(Long batchId, Long subjectId);

    Optional<Marks> findByStudentIdAndSubjectIdAndExamTypeAndAcademicYear(
            Long studentId, Long subjectId, Marks.ExamType examType, String year);

    @Query("SELECT AVG(m.marksObtained / m.maxMarks * 100) FROM Marks m " +
           "WHERE m.studentId = :sid AND m.semester = :sem")
    Double avgPercentageBySemester(@Param("sid") Long studentId, @Param("sem") Integer semester);

    @Modifying
    void deleteByStudentId(Long studentId);

    @Modifying
    void deleteBySubjectId(Long subjectId);
}
