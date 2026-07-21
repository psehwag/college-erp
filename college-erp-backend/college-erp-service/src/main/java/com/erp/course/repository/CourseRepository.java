package com.erp.course.repository;
import com.erp.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByDepartmentIdAndIsActiveTrue(Long deptId);
    List<Course> findByDepartmentId(Long deptId);
    boolean existsByCode(String code);
}
