package com.erp.faculty.repository;

import com.erp.faculty.entity.FacultyAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacultyAssignmentRepository extends JpaRepository<FacultyAssignment, Long> {
    List<FacultyAssignment> findByFacultyIdAndIsActiveTrue(Long facultyId);
    List<FacultyAssignment> findByBatchIdAndIsActiveTrue(Long batchId);
    List<FacultyAssignment> findBySubjectIdAndIsActiveTrue(Long subjectId);
    boolean existsByFacultyIdAndSubjectIdAndBatchIdAndAcademicYear(
            Long fId, Long sId, Long bId, String year);
}
