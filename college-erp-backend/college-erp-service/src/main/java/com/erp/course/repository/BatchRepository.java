package com.erp.course.repository;
import com.erp.course.entity.Batch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface BatchRepository extends JpaRepository<Batch, Long> {
    List<Batch> findByCourseIdAndIsActiveTrue(Long courseId);
    List<Batch> findByDepartmentIdAndIsActiveTrue(Long deptId);
    List<Batch> findByCourseId(Long courseId);
    List<Batch> findByDepartmentId(Long deptId);
}
