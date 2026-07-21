package com.erp.student.dto;

import com.erp.student.entity.Student;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class StudentDto {

    // ── Create request ────────────────────────────────────────────────────

    public static class CreateRequest {
        @NotBlank(message = "First name is required")
        private String firstName;

        @NotBlank(message = "Last name is required")
        private String lastName;

        @Email(message = "Invalid email")
        @NotBlank(message = "Email is required")
        private String email;

        private String phone;
        private LocalDate dateOfBirth;
        private Student.Gender gender;
        private String address;

        @NotNull(message = "Department is required")
        private Long departmentId;

        @NotNull(message = "Course is required")
        private Long courseId;

        private Long batchId;

        @NotNull(message = "Semester is required")
        @Min(1) @Max(10)
        private Integer currentSemester;

        @NotNull(message = "Admission year is required")
        private Integer admissionYear;

        private Long parentId;

        public CreateRequest() {}

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public LocalDate getDateOfBirth() { return dateOfBirth; }
        public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
        public Student.Gender getGender() { return gender; }
        public void setGender(Student.Gender gender) { this.gender = gender; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public Long getDepartmentId() { return departmentId; }
        public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }
        public Long getBatchId() { return batchId; }
        public void setBatchId(Long batchId) { this.batchId = batchId; }
        public Integer getCurrentSemester() { return currentSemester; }
        public void setCurrentSemester(Integer currentSemester) { this.currentSemester = currentSemester; }
        public Integer getAdmissionYear() { return admissionYear; }
        public void setAdmissionYear(Integer admissionYear) { this.admissionYear = admissionYear; }
        public Long getParentId() { return parentId; }
        public void setParentId(Long parentId) { this.parentId = parentId; }
    }

    // ── Update request ────────────────────────────────────────────────────

    public static class UpdateRequest {
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private LocalDate dateOfBirth;
        private Student.Gender gender;
        private String address;
        private Long departmentId;
        private Long courseId;
        private Long batchId;
        private Integer currentSemester;
        private Integer admissionYear;
        private Long parentId;
        private boolean parentIdProvided = false;
        private Student.StudentStatus status;

        public UpdateRequest() {}

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public LocalDate getDateOfBirth() { return dateOfBirth; }
        public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
        public Student.Gender getGender() { return gender; }
        public void setGender(Student.Gender gender) { this.gender = gender; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public Long getDepartmentId() { return departmentId; }
        public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }
        public Long getBatchId() { return batchId; }
        public void setBatchId(Long batchId) { this.batchId = batchId; }
        public Integer getCurrentSemester() { return currentSemester; }
        public void setCurrentSemester(Integer currentSemester) { this.currentSemester = currentSemester; }
        public Integer getAdmissionYear() { return admissionYear; }
        public void setAdmissionYear(Integer admissionYear) { this.admissionYear = admissionYear; }
        public Long getParentId() { return parentId; }
        public void setParentId(Long parentId) { this.parentId = parentId; this.parentIdProvided = true; }
        public boolean isParentIdProvided() { return parentIdProvided; }
        public Student.StudentStatus getStatus() { return status; }
        public void setStatus(Student.StudentStatus status) { this.status = status; }
    }

    // ── Response ──────────────────────────────────────────────────────────

    public static class Response {
        private Long id;
        private String enrollmentNumber;
        private String firstName;
        private String lastName;
        private String fullName;
        private String email;
        private String phone;
        private LocalDate dateOfBirth;
        private Student.Gender gender;
        private String address;
        private String photoUrl;
        private Long departmentId;
        private Long courseId;
        private Long batchId;
        private Integer currentSemester;
        private Integer admissionYear;
        private Long parentId;
        private Student.StudentStatus status;
        private Boolean faceEnrolled;
        private Long userId;
        private String loginUsername;
        private LocalDateTime createdAt;

        public Response() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getEnrollmentNumber() { return enrollmentNumber; }
        public void setEnrollmentNumber(String enrollmentNumber) { this.enrollmentNumber = enrollmentNumber; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public LocalDate getDateOfBirth() { return dateOfBirth; }
        public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
        public Student.Gender getGender() { return gender; }
        public void setGender(Student.Gender gender) { this.gender = gender; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getPhotoUrl() { return photoUrl; }
        public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
        public Long getDepartmentId() { return departmentId; }
        public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }
        public Long getBatchId() { return batchId; }
        public void setBatchId(Long batchId) { this.batchId = batchId; }
        public Integer getCurrentSemester() { return currentSemester; }
        public void setCurrentSemester(Integer currentSemester) { this.currentSemester = currentSemester; }
        public Integer getAdmissionYear() { return admissionYear; }
        public void setAdmissionYear(Integer admissionYear) { this.admissionYear = admissionYear; }
        public Long getParentId() { return parentId; }
        public void setParentId(Long parentId) { this.parentId = parentId; }
        public Student.StudentStatus getStatus() { return status; }
        public void setStatus(Student.StudentStatus status) { this.status = status; }
        public Boolean getFaceEnrolled() { return faceEnrolled; }
        public void setFaceEnrolled(Boolean faceEnrolled) { this.faceEnrolled = faceEnrolled; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getLoginUsername() { return loginUsername; }
        public void setLoginUsername(String loginUsername) { this.loginUsername = loginUsername; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    // ── Summary (used in list views) ──────────────────────────────────────

    public static class Summary {
        private Long id;
        private String enrollmentNumber;
        private String fullName;
        private String email;
        private Long departmentId;
        private Integer currentSemester;
        private Student.StudentStatus status;

        public Summary() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getEnrollmentNumber() { return enrollmentNumber; }
        public void setEnrollmentNumber(String en) { this.enrollmentNumber = en; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public Long getDepartmentId() { return departmentId; }
        public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
        public Integer getCurrentSemester() { return currentSemester; }
        public void setCurrentSemester(Integer currentSemester) { this.currentSemester = currentSemester; }
        public Student.StudentStatus getStatus() { return status; }
        public void setStatus(Student.StudentStatus status) { this.status = status; }
    }
}
