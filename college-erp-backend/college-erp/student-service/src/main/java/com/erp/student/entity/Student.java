package com.erp.student.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "students")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "enrollment_number", unique = true, nullable = false, length = 20)
    private String enrollmentNumber;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(length = 15)
    private String phone;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(length = 500)
    private String address;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "batch_id")
    private Long batchId;

    @Column(name = "current_semester")
    private Integer currentSemester;

    @Column(name = "admission_year")
    private Integer admissionYear;

    @Column(name = "parent_id")
    private Long parentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private StudentStatus status = StudentStatus.ACTIVE;

    @Column(name = "face_enrolled")
    @Builder.Default
    private Boolean faceEnrolled = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Gender { MALE, FEMALE, OTHER }

    public enum StudentStatus { ACTIVE, INACTIVE, GRADUATED, DROPPED }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
