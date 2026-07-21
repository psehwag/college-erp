package com.erp.student.repository;

import com.erp.student.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByEnrollmentNumber(String en);
    Optional<Student> findByEmail(String email);
    boolean existsByEmail(String email);

    List<Student> findByDepartmentIdAndStatus(Long deptId, Student.StudentStatus status);
    List<Student> findByDepartmentId(Long deptId);
    List<Student> findByCourseIdAndStatus(Long courseId, Student.StudentStatus status);
    List<Student> findByBatchId(Long batchId);
    List<Student> findByParentId(Long parentId);

    @Query("SELECT s FROM Student s WHERE s.status = 'ACTIVE' AND (" +
           "LOWER(s.firstName) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(s.lastName)  LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(s.enrollmentNumber) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(s.email) LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<Student> search(@Param("q") String q, Pageable pageable);

    @Query("SELECT COUNT(s) FROM Student s WHERE s.status = 'ACTIVE'")
    long countActive();

    @Query("SELECT COUNT(s) FROM Student s WHERE s.departmentId = :deptId AND s.status = 'ACTIVE'")
    long countActiveByDept(@Param("deptId") Long deptId);
}
