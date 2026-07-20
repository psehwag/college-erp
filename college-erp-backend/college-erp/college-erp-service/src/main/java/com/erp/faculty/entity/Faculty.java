package com.erp.faculty.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "faculty")
public class Faculty {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Column(name = "department_id")
    private Long departmentId;

    @Column(length = 100)
    private String designation;

    @Column(length = 200)
    private String qualification;

    @Column(length = 200)
    private String specialization;

    @Column(name = "joining_date")
    private LocalDate joiningDate;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Enumerated(EnumType.STRING)
    private FacultyStatus status = FacultyStatus.ACTIVE;

    @Column(name = "user_id")
    private Long userId;

    @CreationTimestamp @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name = "updated_at") private LocalDateTime updatedAt;

    public Faculty() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String v) { this.employeeId = v; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String v) { this.firstName = v; }
    public String getLastName() { return lastName; }
    public void setLastName(String v) { this.lastName = v; }
    public String getFullName() { return firstName + " " + lastName; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
    public String getPhone() { return phone; }
    public void setPhone(String v) { this.phone = v; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate v) { this.dateOfBirth = v; }
    public Gender getGender() { return gender; }
    public void setGender(Gender v) { this.gender = v; }
    public String getAddress() { return address; }
    public void setAddress(String v) { this.address = v; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long v) { this.departmentId = v; }
    public String getDesignation() { return designation; }
    public void setDesignation(String v) { this.designation = v; }
    public String getQualification() { return qualification; }
    public void setQualification(String v) { this.qualification = v; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String v) { this.specialization = v; }
    public LocalDate getJoiningDate() { return joiningDate; }
    public void setJoiningDate(LocalDate v) { this.joiningDate = v; }
    public Integer getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Integer v) { this.experienceYears = v; }
    public FacultyStatus getStatus() { return status; }
    public void setStatus(FacultyStatus v) { this.status = v; }
    public Long getUserId() { return userId; }
    public void setUserId(Long v) { this.userId = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public enum Gender { MALE, FEMALE, OTHER }
    public enum FacultyStatus { ACTIVE, INACTIVE, ON_LEAVE }
}
