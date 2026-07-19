package com.erp.faculty.repository;

import com.erp.faculty.entity.FacultySubjectAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacultySubjectAssignmentRepository extends JpaRepository<FacultySubjectAssignment, Long> {
    List<FacultySubjectAssignment> findByFacultyIdAndIsActiveTrue(Long facultyId);
    List<FacultySubjectAssignment> findBySubjectIdAndIsActiveTrue(Long subjectId);
    List<FacultySubjectAssignment> findByBatchIdAndIsActiveTrue(Long batchId);
    boolean existsByFacultyIdAndSubjectIdAndBatchIdAndAcademicYear(Long f, Long s, Long b, String y);
}
