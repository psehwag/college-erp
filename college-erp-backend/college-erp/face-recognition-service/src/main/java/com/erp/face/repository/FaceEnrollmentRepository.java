package com.erp.face.repository;

import com.erp.face.entity.FaceEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaceEnrollmentRepository extends JpaRepository<FaceEnrollment, Long> {

    List<FaceEnrollment> findByStudentIdAndIsActiveTrue(Long studentId);
    boolean existsByStudentIdAndIsActiveTrue(Long studentId);

    @Modifying
    @Query("UPDATE FaceEnrollment f SET f.isActive = false WHERE f.studentId = :studentId")
    void deactivateByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT COUNT(DISTINCT f.studentId) FROM FaceEnrollment f WHERE f.isActive = true")
    Long countEnrolledStudents();
}
