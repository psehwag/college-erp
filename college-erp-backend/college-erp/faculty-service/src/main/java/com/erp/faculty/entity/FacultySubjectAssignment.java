package com.erp.faculty.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "faculty_subject_assignments",
       uniqueConstraints = @UniqueConstraint(columnNames = {"faculty_id","subject_id","batch_id","academic_year"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FacultySubjectAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "faculty_id", nullable = false)
    private Long facultyId;

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(name = "academic_year", nullable = false, length = 10)
    private String academicYear;   // e.g. "2024-25"

    @Column(name = "semester", nullable = false)
    private Integer semester;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "assigned_at", updatable = false)
    private LocalDateTime assignedAt;
}
