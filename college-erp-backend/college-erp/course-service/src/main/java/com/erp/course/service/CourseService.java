package com.erp.course.service;

import com.erp.course.dto.CourseDto;
import com.erp.course.entity.*;
import com.erp.course.exception.CourseException;
import com.erp.course.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor @Slf4j @Transactional
public class CourseService {

    private final DepartmentRepository deptRepo;
    private final CourseRepository courseRepo;
    private final SubjectRepository subjectRepo;
    private final BatchRepository batchRepo;

    // ── Department ────────────────────────────────────────────────────────

    public CourseDto.DeptResponse createDepartment(CourseDto.DeptCreateRequest req) {
        if (deptRepo.existsByCode(req.getCode())) throw new CourseException("Department code already exists");
        if (deptRepo.existsByName(req.getName())) throw new CourseException("Department name already exists");
        Department d = Department.builder().name(req.getName()).code(req.getCode())
                .description(req.getDescription()).headFacultyId(req.getHeadFacultyId()).build();
        return toDeptResponse(deptRepo.save(d));
    }

    @Transactional(readOnly = true)
    public List<CourseDto.DeptResponse> getAllDepartments() {
        return deptRepo.findByIsActiveTrue().stream().map(this::toDeptResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CourseDto.DeptResponse getDepartmentById(Long id) {
        return deptRepo.findById(id).map(this::toDeptResponse)
                .orElseThrow(() -> new CourseException("Department not found: " + id));
    }

    public CourseDto.DeptResponse updateDepartment(Long id, CourseDto.DeptCreateRequest req) {
        Department d = deptRepo.findById(id).orElseThrow(() -> new CourseException("Department not found"));
        if (req.getName() != null) d.setName(req.getName());
        if (req.getDescription() != null) d.setDescription(req.getDescription());
        if (req.getHeadFacultyId() != null) d.setHeadFacultyId(req.getHeadFacultyId());
        return toDeptResponse(deptRepo.save(d));
    }

    // ── Course ────────────────────────────────────────────────────────────

    public CourseDto.CourseResponse createCourse(CourseDto.CourseCreateRequest req) {
        if (courseRepo.existsByCode(req.getCode())) throw new CourseException("Course code already exists");
        if (!deptRepo.existsById(req.getDepartmentId())) throw new CourseException("Department not found");
        Course c = Course.builder().name(req.getName()).code(req.getCode())
                .description(req.getDescription()).departmentId(req.getDepartmentId())
                .totalSemesters(req.getTotalSemesters()).durationYears(req.getDurationYears())
                .type(req.getType()).build();
        return toCourseResponse(courseRepo.save(c));
    }

    @Transactional(readOnly = true)
    public List<CourseDto.CourseResponse> getCoursesByDepartment(Long deptId) {
        return courseRepo.findByDepartmentIdAndIsActiveTrue(deptId)
                .stream().map(this::toCourseResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CourseDto.CourseResponse getCourseById(Long id) {
        return courseRepo.findById(id).map(this::toCourseResponse)
                .orElseThrow(() -> new CourseException("Course not found: " + id));
    }

    // ── Subject ───────────────────────────────────────────────────────────

    public CourseDto.SubjectResponse createSubject(CourseDto.SubjectCreateRequest req) {
        if (subjectRepo.existsByCode(req.getCode())) throw new CourseException("Subject code already exists");
        Subject s = Subject.builder().name(req.getName()).code(req.getCode())
                .description(req.getDescription()).courseId(req.getCourseId())
                .departmentId(req.getDepartmentId()).semester(req.getSemester())
                .credits(req.getCredits()).totalLectures(req.getTotalLectures()).type(req.getType()).build();
        return toSubjectResponse(subjectRepo.save(s));
    }

    @Transactional(readOnly = true)
    public List<CourseDto.SubjectResponse> getSubjectsByCourseAndSemester(Long courseId, Integer semester) {
        return subjectRepo.findByCourseIdAndSemesterAndIsActiveTrue(courseId, semester)
                .stream().map(this::toSubjectResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseDto.SubjectResponse> getSubjectsByCourse(Long courseId) {
        return subjectRepo.findByCourseIdAndIsActiveTrue(courseId)
                .stream().map(this::toSubjectResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CourseDto.SubjectResponse getSubjectById(Long id) {
        return subjectRepo.findById(id).map(this::toSubjectResponse)
                .orElseThrow(() -> new CourseException("Subject not found: " + id));
    }

    // ── Batch ─────────────────────────────────────────────────────────────

    public CourseDto.BatchResponse createBatch(CourseDto.BatchCreateRequest req) {
        Batch b = Batch.builder().name(req.getName()).courseId(req.getCourseId())
                .departmentId(req.getDepartmentId()).academicYear(req.getAcademicYear())
                .currentSemester(req.getCurrentSemester()).startDate(req.getStartDate())
                .endDate(req.getEndDate()).maxStrength(req.getMaxStrength()).build();
        return toBatchResponse(batchRepo.save(b));
    }

    @Transactional(readOnly = true)
    public List<CourseDto.BatchResponse> getBatchesByCourse(Long courseId) {
        return batchRepo.findByCourseIdAndIsActiveTrue(courseId)
                .stream().map(this::toBatchResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseDto.BatchResponse> getBatchesByDepartment(Long deptId) {
        return batchRepo.findByDepartmentIdAndIsActiveTrue(deptId)
                .stream().map(this::toBatchResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CourseDto.BatchResponse getBatchById(Long id) {
        return batchRepo.findById(id).map(this::toBatchResponse)
                .orElseThrow(() -> new CourseException("Batch not found: " + id));
    }

    public CourseDto.BatchResponse updateBatchSemester(Long batchId, Integer semester) {
        Batch b = batchRepo.findById(batchId).orElseThrow(() -> new CourseException("Batch not found"));
        b.setCurrentSemester(semester);
        return toBatchResponse(batchRepo.save(b));
    }

    // ── Mappers ───────────────────────────────────────────────────────────

    private CourseDto.DeptResponse toDeptResponse(Department d) {
        return CourseDto.DeptResponse.builder().id(d.getId()).name(d.getName()).code(d.getCode())
                .description(d.getDescription()).headFacultyId(d.getHeadFacultyId())
                .isActive(d.getIsActive()).createdAt(d.getCreatedAt()).build();
    }

    private CourseDto.CourseResponse toCourseResponse(Course c) {
        return CourseDto.CourseResponse.builder().id(c.getId()).name(c.getName()).code(c.getCode())
                .description(c.getDescription()).departmentId(c.getDepartmentId())
                .totalSemesters(c.getTotalSemesters()).durationYears(c.getDurationYears())
                .type(c.getType()).isActive(c.getIsActive()).createdAt(c.getCreatedAt()).build();
    }

    private CourseDto.SubjectResponse toSubjectResponse(Subject s) {
        return CourseDto.SubjectResponse.builder().id(s.getId()).name(s.getName()).code(s.getCode())
                .description(s.getDescription()).courseId(s.getCourseId()).departmentId(s.getDepartmentId())
                .semester(s.getSemester()).credits(s.getCredits()).totalLectures(s.getTotalLectures())
                .type(s.getType()).isActive(s.getIsActive()).createdAt(s.getCreatedAt()).build();
    }

    private CourseDto.BatchResponse toBatchResponse(Batch b) {
        return CourseDto.BatchResponse.builder().id(b.getId()).name(b.getName())
                .courseId(b.getCourseId()).departmentId(b.getDepartmentId())
                .academicYear(b.getAcademicYear()).currentSemester(b.getCurrentSemester())
                .startDate(b.getStartDate()).endDate(b.getEndDate()).maxStrength(b.getMaxStrength())
                .isActive(b.getIsActive()).createdAt(b.getCreatedAt()).build();
    }
}
