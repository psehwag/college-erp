package com.erp.course.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "courses")
public class Course {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false, length = 100) private String name;
    @Column(unique = true, nullable = false, length = 20)  private String code;
    @Column(length = 500) private String description;
    @Column(name = "department_id", nullable = false) private Long departmentId;
    @Column(name = "total_semesters", nullable = false) private Integer totalSemesters;
    @Column(name = "duration_years") private Integer durationYears;
    @Enumerated(EnumType.STRING) private CourseType type;
    @Column(name = "is_active") private Boolean isActive = true;
    @CreationTimestamp @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;

    public Course() {}
    public Long getId() { return id; }
    public void setId(Long v) { this.id = v; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getCode() { return code; }
    public void setCode(String v) { this.code = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long v) { this.departmentId = v; }
    public Integer getTotalSemesters() { return totalSemesters; }
    public void setTotalSemesters(Integer v) { this.totalSemesters = v; }
    public Integer getDurationYears() { return durationYears; }
    public void setDurationYears(Integer v) { this.durationYears = v; }
    public CourseType getType() { return type; }
    public void setType(CourseType v) { this.type = v; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean v) { this.isActive = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public enum CourseType { UNDERGRADUATE, POSTGRADUATE, DIPLOMA, CERTIFICATE }
}
