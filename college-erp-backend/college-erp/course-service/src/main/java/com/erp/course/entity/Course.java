package com.erp.course.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity @Table(name = "courses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Course {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String name;

    @Column(unique = true, nullable = false, length = 20)
    private String code;

    @Column(length = 500)
    private String description;

    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    @Column(name = "total_semesters", nullable = false)
    private Integer totalSemesters;

    @Column(name = "duration_years")
    private Integer durationYears;

    @Enumerated(EnumType.STRING)
    private CourseType type;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum CourseType { UNDERGRADUATE, POSTGRADUATE, DIPLOMA, CERTIFICATE }
}
