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

    Optional<Student> findByEnrollmentNumber(String enrollmentNumber);
    Optional<Student> findByEmail(String email);
    boolean existsByEnrollmentNumber(String enrollmentNumber);
    boolean existsByEmail(String email);

    List<Student> findByDepartmentId(Long departmentId);
    List<Student> findByCourseId(Long courseId);
    List<Student> findByBatchId(Long batchId);
    List<Student> findByParentId(Long parentId);

    Page<Student> findByStatus(Student.StudentStatus status, Pageable pageable);

    @Query("SELECT s FROM Student s WHERE s.departmentId = :deptId AND s.currentSemester = :semester")
    List<Student> findByDepartmentAndSemester(@Param("deptId") Long deptId, @Param("semester") Integer semester);

    @Query("SELECT COUNT(s) FROM Student s WHERE s.status = 'ACTIVE'")
    Long countActiveStudents();

    @Query("SELECT COUNT(s) FROM Student s WHERE s.departmentId = :deptId AND s.status = 'ACTIVE'")
    Long countActiveStudentsByDepartment(@Param("deptId") Long deptId);

    @Query("SELECT s FROM Student s WHERE " +
           "LOWER(s.firstName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(s.lastName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(s.enrollmentNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(s.email) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<Student> search(@Param("q") String query, Pageable pageable);
}
