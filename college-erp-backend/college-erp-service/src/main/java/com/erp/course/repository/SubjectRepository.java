package com.erp.course.repository;
import com.erp.course.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findByCourseIdAndIsActiveTrue(Long courseId);
    List<Subject> findByCourseIdAndSemesterAndIsActiveTrue(Long courseId, Integer semester);
    List<Subject> findByCourseId(Long courseId);
    boolean existsByCode(String code);
}
