package com.erp.faculty.repository;

import com.erp.faculty.entity.Faculty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    Optional<Faculty> findByEmployeeId(String employeeId);
    Optional<Faculty> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Faculty> findByDepartmentIdAndStatus(Long deptId, Faculty.FacultyStatus status);
    List<Faculty> findByDepartmentId(Long deptId);

    @Query("SELECT f FROM Faculty f WHERE " +
           "LOWER(f.firstName) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(f.lastName)  LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(f.employeeId) LIKE LOWER(CONCAT('%',:q,'%'))")
    Page<Faculty> search(@Param("q") String q, Pageable pageable);

    @Query("SELECT COUNT(f) FROM Faculty f WHERE f.status = 'ACTIVE'")
    long countActive();
}
