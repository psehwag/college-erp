package com.erp.course.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity @Table(name = "subjects")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Subject {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(unique = true, nullable = false, length = 20)
    private String code;

    @Column(length = 500)
    private String description;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    @Column(name = "semester", nullable = false)
    private Integer semester;

    @Column(name = "credits", nullable = false)
    private Integer credits;

    @Column(name = "total_lectures")
    private Integer totalLectures;

    @Enumerated(EnumType.STRING)
    private SubjectType type;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum SubjectType { THEORY, PRACTICAL, ELECTIVE }
}
