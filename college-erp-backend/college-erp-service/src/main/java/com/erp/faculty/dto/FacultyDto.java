package com.erp.faculty.dto;

import com.erp.faculty.entity.Faculty;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class FacultyDto {

    public static class CreateRequest {
        @NotBlank private String firstName;
        @NotBlank private String lastName;
        @Email @NotBlank private String email;
        private String phone;
        private LocalDate dateOfBirth;
        private Faculty.Gender gender;
        private String address;
        @NotNull private Long departmentId;
        private String designation;
        private String qualification;
        private String specialization;
        private LocalDate joiningDate;
        private Integer experienceYears;

        public CreateRequest() {}
        public String getFirstName() { return firstName; }
        public void setFirstName(String v) { this.firstName = v; }
        public String getLastName() { return lastName; }
        public void setLastName(String v) { this.lastName = v; }
        public String getEmail() { return email; }
        public void setEmail(String v) { this.email = v; }
        public String getPhone() { return phone; }
        public void setPhone(String v) { this.phone = v; }
        public LocalDate getDateOfBirth() { return dateOfBirth; }
        public void setDateOfBirth(LocalDate v) { this.dateOfBirth = v; }
        public Faculty.Gender getGender() { return gender; }
        public void setGender(Faculty.Gender v) { this.gender = v; }
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
    }

    public static class UpdateRequest {
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String address;
        private Long departmentId;
        private String designation;
        private String qualification;
        private String specialization;
        private Integer experienceYears;
        private Faculty.FacultyStatus status;

        public UpdateRequest() {}
        public String getFirstName() { return firstName; }
        public void setFirstName(String v) { this.firstName = v; }
        public String getLastName() { return lastName; }
        public void setLastName(String v) { this.lastName = v; }
        public String getEmail() { return email; }
        public void setEmail(String v) { this.email = v; }
        public String getPhone() { return phone; }
        public void setPhone(String v) { this.phone = v; }
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
        public Integer getExperienceYears() { return experienceYears; }
        public void setExperienceYears(Integer v) { this.experienceYears = v; }
        public Faculty.FacultyStatus getStatus() { return status; }
        public void setStatus(Faculty.FacultyStatus v) { this.status = v; }
    }

    public static class Response {
        private Long id;
        private String employeeId;
        private String firstName;
        private String lastName;
        private String fullName;
        private String email;
        private String phone;
        private LocalDate dateOfBirth;
        private Faculty.Gender gender;
        private String address;
        private Long departmentId;
        private String designation;
        private String qualification;
        private String specialization;
        private LocalDate joiningDate;
        private Integer experienceYears;
        private Faculty.FacultyStatus status;
        private Long userId;
        private String loginUsername;
        private LocalDateTime createdAt;

        public Response() {}

        public Long getId() { return id; }
        public void setId(Long v) { this.id = v; }
        public String getEmployeeId() { return employeeId; }
        public void setEmployeeId(String v) { this.employeeId = v; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String v) { this.firstName = v; }
        public String getLastName() { return lastName; }
        public void setLastName(String v) { this.lastName = v; }
        public String getFullName() { return fullName; }
        public void setFullName(String v) { this.fullName = v; }
        public String getEmail() { return email; }
        public void setEmail(String v) { this.email = v; }
        public String getPhone() { return phone; }
        public void setPhone(String v) { this.phone = v; }
        public LocalDate getDateOfBirth() { return dateOfBirth; }
        public void setDateOfBirth(LocalDate v) { this.dateOfBirth = v; }
        public Faculty.Gender getGender() { return gender; }
        public void setGender(Faculty.Gender v) { this.gender = v; }
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
        public Faculty.FacultyStatus getStatus() { return status; }
        public void setStatus(Faculty.FacultyStatus v) { this.status = v; }
        public Long getUserId() { return userId; }
        public void setUserId(Long v) { this.userId = v; }
        public String getLoginUsername() { return loginUsername; }
        public void setLoginUsername(String v) { this.loginUsername = v; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
    }

    public static class AssignRequest {
        @NotNull private Long facultyId;
        @NotNull private Long subjectId;
        @NotNull private Long batchId;
        @NotBlank private String academicYear;
        @NotNull private Integer semester;
        private String description;

        public AssignRequest() {}
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
        public String getDescription() { return description; }
        public void setDescription(String v) { this.description = v; }
    }

    public static class AssignUpdateRequest {
        private String description;
        public AssignUpdateRequest() {}
        public String getDescription() { return description; }
        public void setDescription(String v) { this.description = v; }
    }

    public static class AssignResponse {
        private Long id;
        private Long facultyId;
        private Long subjectId;
        private Long batchId;
        private String academicYear;
        private Integer semester;
        private Boolean isActive;
        private String description;
        private LocalDateTime assignedAt;

        public AssignResponse() {}
        public Long getId() { return id; }
        public void setId(Long v) { this.id = v; }
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
        public void setAssignedAt(LocalDateTime v) { this.assignedAt = v; }
    }
}
