package com.erp.faculty.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "faculty_assignments",
       uniqueConstraints = @UniqueConstraint(
               columnNames = {"faculty_id","subject_id","batch_id","academic_year"}))
public class FacultyAssignment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "faculty_id", nullable = false)
    private Long facultyId;

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(name = "academic_year", nullable = false, length = 10)
    private String academicYear;

    @Column(name = "semester", nullable = false)
    private Integer semester;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "description", length = 1000)
    private String description;

    @CreationTimestamp @Column(name = "assigned_at", updatable = false)
    private LocalDateTime assignedAt;

    public FacultyAssignment() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFacultyId() { return facultyId; }
    public void setFacultyId(Long v) { this.facultyId = v; }
    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long v) { this.subjectId = v; }
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long v) { this.batchId = v; }
    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String v) { this.academicYear = v; }
    public Integer getSemester() { return semester; }
    public void setSemester(Integer v) { this.semester = v; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean v) { this.isActive = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
}
