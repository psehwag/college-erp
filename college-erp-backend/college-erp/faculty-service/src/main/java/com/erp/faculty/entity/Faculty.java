package com.erp.faculty.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "faculty")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Faculty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", unique = true, nullable = false, length = 20)
    private String employeeId;

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

    @Column(name = "designation", length = 100)
    private String designation;

    @Column(name = "qualification", length = 200)
    private String qualification;

    @Column(name = "specialization", length = 200)
    private String specialization;

    @Column(name = "joining_date")
    private LocalDate joiningDate;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private FacultyStatus status = FacultyStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Gender { MALE, FEMALE, OTHER }
    public enum FacultyStatus { ACTIVE, INACTIVE, ON_LEAVE }

    public String getFullName() { return firstName + " " + lastName; }
}
