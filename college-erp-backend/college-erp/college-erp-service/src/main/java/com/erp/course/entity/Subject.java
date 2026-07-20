package com.erp.course.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "subjects")
public class Subject {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 150) private String name;
    @Column(unique = true, nullable = false, length = 20) private String code;
    @Column(length = 500) private String description;
    @Column(name = "course_id", nullable = false) private Long courseId;
    @Column(name = "department_id", nullable = false) private Long departmentId;
    @Column(name = "semester", nullable = false) private Integer semester;
    @Column(name = "credits", nullable = false) private Integer credits;
    @Column(name = "total_lectures") private Integer totalLectures;
    @Enumerated(EnumType.STRING) private SubjectType type;
    @Column(name = "is_active") private Boolean isActive = true;
    @CreationTimestamp @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;

    public Subject() {}
    public Long getId() { return id; }
    public void setId(Long v) { this.id = v; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getCode() { return code; }
    public void setCode(String v) { this.code = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long v) { this.courseId = v; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long v) { this.departmentId = v; }
    public Integer getSemester() { return semester; }
    public void setSemester(Integer v) { this.semester = v; }
    public Integer getCredits() { return credits; }
    public void setCredits(Integer v) { this.credits = v; }
    public Integer getTotalLectures() { return totalLectures; }
    public void setTotalLectures(Integer v) { this.totalLectures = v; }
    public SubjectType getType() { return type; }
    public void setType(SubjectType v) { this.type = v; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean v) { this.isActive = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public enum SubjectType { THEORY, PRACTICAL, ELECTIVE }
}
