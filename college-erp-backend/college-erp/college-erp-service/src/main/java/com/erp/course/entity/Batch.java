package com.erp.course.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "batches")
public class Batch {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 50) private String name;
    @Column(name = "course_id", nullable = false) private Long courseId;
    @Column(name = "department_id", nullable = false) private Long departmentId;
    @Column(name = "academic_year", nullable = false, length = 10) private String academicYear;
    @Column(name = "current_semester") private Integer currentSemester;
    @Column(name = "start_date") private LocalDate startDate;
    @Column(name = "end_date") private LocalDate endDate;
    @Column(name = "max_strength") private Integer maxStrength;
    @Column(name = "is_active") private Boolean isActive = true;
    @CreationTimestamp @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;

    public Batch() {}
    public Long getId() { return id; }
    public void setId(Long v) { this.id = v; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long v) { this.courseId = v; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long v) { this.departmentId = v; }
    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String v) { this.academicYear = v; }
    public Integer getCurrentSemester() { return currentSemester; }
    public void setCurrentSemester(Integer v) { this.currentSemester = v; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate v) { this.startDate = v; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate v) { this.endDate = v; }
    public Integer getMaxStrength() { return maxStrength; }
    public void setMaxStrength(Integer v) { this.maxStrength = v; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean v) { this.isActive = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
