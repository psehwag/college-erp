package com.erp.course.repository;
import com.erp.course.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByIsActiveTrue();
    boolean existsByCode(String code);
    boolean existsByName(String name);
    Optional<Department> findByCode(String code);
}
