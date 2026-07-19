package com.erp.course.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name = "batches")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Batch {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;         // e.g. "CS-A-2024"

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    @Column(name = "academic_year", nullable = false, length = 10)
    private String academicYear; // e.g. "2024-25"

    @Column(name = "current_semester")
    private Integer currentSemester;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "max_strength")
    private Integer maxStrength;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
