package com.erp.faculty.service;

import com.erp.faculty.dto.FacultyDto;
import com.erp.faculty.entity.Faculty;
import com.erp.faculty.entity.FacultySubjectAssignment;
import com.erp.faculty.exception.FacultyException;
import com.erp.faculty.repository.FacultyRepository;
import com.erp.faculty.repository.FacultySubjectAssignmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FacultyService {

    private final FacultyRepository facultyRepository;
    private final FacultySubjectAssignmentRepository assignmentRepository;

    public FacultyDto.Response createFaculty(FacultyDto.CreateRequest req) {
        if (facultyRepository.existsByEmail(req.getEmail()))
            throw new FacultyException("Email already registered");

        String empId = generateEmployeeId(req.getDepartmentId());
        Faculty faculty = Faculty.builder()
                .employeeId(empId).firstName(req.getFirstName()).lastName(req.getLastName())
                .email(req.getEmail()).phone(req.getPhone()).dateOfBirth(req.getDateOfBirth())
                .gender(req.getGender()).address(req.getAddress()).departmentId(req.getDepartmentId())
                .designation(req.getDesignation()).qualification(req.getQualification())
                .specialization(req.getSpecialization()).joiningDate(req.getJoiningDate())
                .experienceYears(req.getExperienceYears()).build();

        faculty = facultyRepository.save(faculty);
        log.info("Faculty created: {}", faculty.getEmployeeId());
        return toResponse(faculty);
    }

    @Transactional(readOnly = true)
    public FacultyDto.Response getById(Long id) {
        return facultyRepository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new FacultyException("Faculty not found: " + id));
    }

    @Transactional(readOnly = true)
    public FacultyDto.Response getByEmployeeId(String empId) {
        return facultyRepository.findByEmployeeId(empId).map(this::toResponse)
                .orElseThrow(() -> new FacultyException("Faculty not found: " + empId));
    }

    public FacultyDto.Response update(Long id, FacultyDto.UpdateRequest req) {
        Faculty f = facultyRepository.findById(id)
                .orElseThrow(() -> new FacultyException("Faculty not found: " + id));
        if (req.getFirstName() != null) f.setFirstName(req.getFirstName());
        if (req.getLastName() != null) f.setLastName(req.getLastName());
        if (req.getPhone() != null) f.setPhone(req.getPhone());
        if (req.getAddress() != null) f.setAddress(req.getAddress());
        if (req.getDesignation() != null) f.setDesignation(req.getDesignation());
        if (req.getQualification() != null) f.setQualification(req.getQualification());
        if (req.getSpecialization() != null) f.setSpecialization(req.getSpecialization());
        if (req.getExperienceYears() != null) f.setExperienceYears(req.getExperienceYears());
        if (req.getStatus() != null) f.setStatus(req.getStatus());
        return toResponse(facultyRepository.save(f));
    }

    public void delete(Long id) {
        Faculty f = facultyRepository.findById(id)
                .orElseThrow(() -> new FacultyException("Faculty not found: " + id));
        f.setStatus(Faculty.FacultyStatus.INACTIVE);
        facultyRepository.save(f);
    }

    @Transactional(readOnly = true)
    public Page<FacultyDto.Response> getAll(int page, int size, String sort) {
        return facultyRepository.findAll(PageRequest.of(page, size, Sort.by(sort)))
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<FacultyDto.Response> search(String q, int page, int size) {
        return facultyRepository.search(q, PageRequest.of(page, size)).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<FacultyDto.Response> getByDepartment(Long deptId) {
        return facultyRepository.findByDepartmentId(deptId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    // ── Subject Assignments ────────────────────────────────────────────────

    public FacultyDto.AssignmentResponse assignSubject(FacultyDto.AssignSubjectRequest req) {
        if (!facultyRepository.existsById(req.getFacultyId()))
            throw new FacultyException("Faculty not found: " + req.getFacultyId());
        if (assignmentRepository.existsByFacultyIdAndSubjectIdAndBatchIdAndAcademicYear(
                req.getFacultyId(), req.getSubjectId(), req.getBatchId(), req.getAcademicYear()))
            throw new FacultyException("Assignment already exists");

        FacultySubjectAssignment a = FacultySubjectAssignment.builder()
                .facultyId(req.getFacultyId()).subjectId(req.getSubjectId())
                .batchId(req.getBatchId()).academicYear(req.getAcademicYear())
                .semester(req.getSemester()).build();
        return toAssignmentResponse(assignmentRepository.save(a));
    }

    @Transactional(readOnly = true)
    public List<FacultyDto.AssignmentResponse> getAssignmentsByFaculty(Long facultyId) {
        return assignmentRepository.findByFacultyIdAndIsActiveTrue(facultyId)
                .stream().map(this::toAssignmentResponse).collect(Collectors.toList());
    }

    public void removeAssignment(Long assignmentId) {
        FacultySubjectAssignment a = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new FacultyException("Assignment not found"));
        a.setIsActive(false);
        assignmentRepository.save(a);
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private String generateEmployeeId(Long deptId) {
        long count = facultyRepository.count() + 1;
        return String.format("FAC%02d%04d", deptId, count);
    }

    private FacultyDto.Response toResponse(Faculty f) {
        return FacultyDto.Response.builder()
                .id(f.getId()).employeeId(f.getEmployeeId())
                .firstName(f.getFirstName()).lastName(f.getLastName()).fullName(f.getFullName())
                .email(f.getEmail()).phone(f.getPhone()).dateOfBirth(f.getDateOfBirth())
                .gender(f.getGender()).address(f.getAddress()).photoUrl(f.getPhotoUrl())
                .departmentId(f.getDepartmentId()).designation(f.getDesignation())
                .qualification(f.getQualification()).specialization(f.getSpecialization())
                .joiningDate(f.getJoiningDate()).experienceYears(f.getExperienceYears())
                .status(f.getStatus()).createdAt(f.getCreatedAt()).build();
    }

    private FacultyDto.AssignmentResponse toAssignmentResponse(FacultySubjectAssignment a) {
        return FacultyDto.AssignmentResponse.builder()
                .id(a.getId()).facultyId(a.getFacultyId()).subjectId(a.getSubjectId())
                .batchId(a.getBatchId()).academicYear(a.getAcademicYear())
                .semester(a.getSemester()).isActive(a.getIsActive()).assignedAt(a.getAssignedAt()).build();
    }
}
