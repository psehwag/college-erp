package com.erp.faculty.service;

import com.erp.auth.entity.User;
import com.erp.auth.repository.UserRepository;
import com.erp.auth.service.AuthService;
import com.erp.common.exception.AppException;
import com.erp.faculty.dto.FacultyDto;
import com.erp.faculty.entity.Faculty;
import com.erp.faculty.entity.FacultyAssignment;
import com.erp.faculty.repository.FacultyAssignmentRepository;
import com.erp.faculty.repository.FacultyRepository;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@Transactional
public class FacultyService {

    private static final Logger log = Logger.getLogger(FacultyService.class.getName());

    private final FacultyRepository facultyRepository;
    private final FacultyAssignmentRepository assignmentRepository;
    private final AuthService authService;
    private final UserRepository userRepository;

    public FacultyService(FacultyRepository facultyRepository,
                          FacultyAssignmentRepository assignmentRepository,
                          AuthService authService,
                          UserRepository userRepository) {
        this.facultyRepository   = facultyRepository;
        this.assignmentRepository = assignmentRepository;
        this.authService         = authService;
        this.userRepository      = userRepository;
    }

    public FacultyDto.Response create(FacultyDto.CreateRequest req) {
        if (facultyRepository.existsByEmail(req.getEmail())) {
            throw new AppException("Email already registered for another faculty", HttpStatus.CONFLICT);
        }

        String empId = generateEmployeeId(req.getDepartmentId());

        Faculty f = new Faculty();
        f.setEmployeeId(empId);
        f.setFirstName(req.getFirstName());
        f.setLastName(req.getLastName());
        f.setEmail(req.getEmail());
        f.setPhone(req.getPhone());
        f.setDateOfBirth(req.getDateOfBirth());
        f.setGender(req.getGender());
        f.setAddress(req.getAddress());
        f.setDepartmentId(req.getDepartmentId());
        f.setDesignation(req.getDesignation());
        f.setQualification(req.getQualification());
        f.setSpecialization(req.getSpecialization());
        f.setJoiningDate(req.getJoiningDate());
        f.setExperienceYears(req.getExperienceYears());
        f.setStatus(Faculty.FacultyStatus.ACTIVE);
        f = facultyRepository.save(f);

        // Auto-create login credentials
        User user = authService.createLinkedUser(empId, req.getEmail(), User.Role.FACULTY, f.getId());
        f.setUserId(user.getId());
        f = facultyRepository.save(f);

        log.info("Faculty created: " + empId + " login=" + user.getUsername());
        return toResponse(f, user.getUsername());
    }

    @Transactional(readOnly = true)
    public FacultyDto.Response getById(Long id) {
        Faculty f = findById(id);
        return toResponse(f, resolveUsername(f.getUserId()));
    }

    @Transactional(readOnly = true)
    public FacultyDto.Response getByEmployeeId(String empId) {
        Faculty f = facultyRepository.findByEmployeeId(empId)
                .orElseThrow(() -> new AppException("Faculty not found: " + empId, HttpStatus.NOT_FOUND));
        return toResponse(f, resolveUsername(f.getUserId()));
    }

    public FacultyDto.Response update(Long id, FacultyDto.UpdateRequest req) {
        Faculty f = findById(id);
        if (req.getFirstName() != null)      f.setFirstName(req.getFirstName());
        if (req.getLastName() != null)        f.setLastName(req.getLastName());
        if (req.getPhone() != null)           f.setPhone(req.getPhone());
        if (req.getAddress() != null)         f.setAddress(req.getAddress());
        if (req.getDesignation() != null)     f.setDesignation(req.getDesignation());
        if (req.getQualification() != null)   f.setQualification(req.getQualification());
        if (req.getSpecialization() != null)  f.setSpecialization(req.getSpecialization());
        if (req.getExperienceYears() != null) f.setExperienceYears(req.getExperienceYears());
        if (req.getStatus() != null) {
            f.setStatus(req.getStatus());
            if (req.getStatus() == Faculty.FacultyStatus.INACTIVE && f.getUserId() != null) {
                authService.setActive(f.getUserId(), false);
            }
        }
        return toResponse(facultyRepository.save(f), resolveUsername(f.getUserId()));
    }

    public void delete(Long id) {
        Faculty f = findById(id);
        f.setStatus(Faculty.FacultyStatus.INACTIVE);
        if (f.getUserId() != null) authService.setActive(f.getUserId(), false);
        facultyRepository.save(f);
    }

    @Transactional(readOnly = true)
    public Page<FacultyDto.Response> getAll(int page, int size, String sort) {
        return facultyRepository.findAll(PageRequest.of(page, size, Sort.by(sort)))
                .map(f -> toResponse(f, resolveUsername(f.getUserId())));
    }

    @Transactional(readOnly = true)
    public Page<FacultyDto.Response> search(String q, int page, int size) {
        return facultyRepository.search(q, PageRequest.of(page, size))
                .map(f -> toResponse(f, resolveUsername(f.getUserId())));
    }

    @Transactional(readOnly = true)
    public List<FacultyDto.Response> getByDepartment(Long deptId) {
        return facultyRepository.findByDepartmentIdAndStatus(deptId, Faculty.FacultyStatus.ACTIVE)
                .stream().map(f -> toResponse(f, resolveUsername(f.getUserId())))
                .collect(Collectors.toList());
    }

    public FacultyDto.AssignResponse assignSubject(FacultyDto.AssignRequest req) {
        if (!facultyRepository.existsById(req.getFacultyId())) {
            throw new AppException("Faculty not found", HttpStatus.NOT_FOUND);
        }
        if (assignmentRepository.existsByFacultyIdAndSubjectIdAndBatchIdAndAcademicYear(
                req.getFacultyId(), req.getSubjectId(), req.getBatchId(), req.getAcademicYear())) {
            throw new AppException("Assignment already exists", HttpStatus.CONFLICT);
        }
        FacultyAssignment a = new FacultyAssignment();
        a.setFacultyId(req.getFacultyId());
        a.setSubjectId(req.getSubjectId());
        a.setBatchId(req.getBatchId());
        a.setAcademicYear(req.getAcademicYear());
        a.setSemester(req.getSemester());
        a.setIsActive(true);
        return toAssignResponse(assignmentRepository.save(a));
    }

    @Transactional(readOnly = true)
    public List<FacultyDto.AssignResponse> getAssignments(Long facultyId) {
        return assignmentRepository.findByFacultyIdAndIsActiveTrue(facultyId)
                .stream().map(this::toAssignResponse).collect(Collectors.toList());
    }

    public void removeAssignment(Long assignmentId) {
        FacultyAssignment a = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AppException("Assignment not found", HttpStatus.NOT_FOUND));
        a.setIsActive(false);
        assignmentRepository.save(a);
    }

    private String generateEmployeeId(Long deptId) {
        long count = facultyRepository.count() + 1;
        return String.format("FAC%02d%04d", deptId, count);
    }

    private Faculty findById(Long id) {
        return facultyRepository.findById(id)
                .orElseThrow(() -> new AppException("Faculty not found: " + id, HttpStatus.NOT_FOUND));
    }

    private String resolveUsername(Long userId) {
        if (userId == null) return null;
        return userRepository.findById(userId).map(User::getUsername).orElse(null);
    }

    private FacultyDto.Response toResponse(Faculty f, String username) {
        FacultyDto.Response r = new FacultyDto.Response();
        r.setId(f.getId());
        r.setEmployeeId(f.getEmployeeId());
        r.setFirstName(f.getFirstName());
        r.setLastName(f.getLastName());
        r.setFullName(f.getFullName());
        r.setEmail(f.getEmail());
        r.setPhone(f.getPhone());
        r.setDateOfBirth(f.getDateOfBirth());
        r.setGender(f.getGender());
        r.setAddress(f.getAddress());
        r.setDepartmentId(f.getDepartmentId());
        r.setDesignation(f.getDesignation());
        r.setQualification(f.getQualification());
        r.setSpecialization(f.getSpecialization());
        r.setJoiningDate(f.getJoiningDate());
        r.setExperienceYears(f.getExperienceYears());
        r.setStatus(f.getStatus());
        r.setUserId(f.getUserId());
        r.setLoginUsername(username);
        r.setCreatedAt(f.getCreatedAt());
        return r;
    }

    private FacultyDto.AssignResponse toAssignResponse(FacultyAssignment a) {
        FacultyDto.AssignResponse r = new FacultyDto.AssignResponse();
        r.setId(a.getId());
        r.setFacultyId(a.getFacultyId());
        r.setSubjectId(a.getSubjectId());
        r.setBatchId(a.getBatchId());
        r.setAcademicYear(a.getAcademicYear());
        r.setSemester(a.getSemester());
        r.setIsActive(a.getIsActive());
        r.setAssignedAt(a.getAssignedAt());
        return r;
    }
}
