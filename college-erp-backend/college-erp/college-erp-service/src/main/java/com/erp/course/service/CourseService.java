package com.erp.course.service;

import com.erp.common.exception.AppException;
import com.erp.course.entity.*;
import com.erp.course.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@Transactional
public class CourseService {

    private static final Logger log = Logger.getLogger(CourseService.class.getName());

    private final DepartmentRepository deptRepo;
    private final CourseRepository courseRepo;
    private final SubjectRepository subjectRepo;
    private final BatchRepository batchRepo;

    public CourseService(DepartmentRepository deptRepo, CourseRepository courseRepo,
                         SubjectRepository subjectRepo, BatchRepository batchRepo) {
        this.deptRepo    = deptRepo;
        this.courseRepo  = courseRepo;
        this.subjectRepo = subjectRepo;
        this.batchRepo   = batchRepo;
    }

    // ── Department ────────────────────────────────────────────────────────

    public Department createDepartment(Map<String, Object> req) {
        String name = (String) req.get("name");
        String code = (String) req.get("code");
        if (deptRepo.existsByCode(code)) throw new AppException("Department code already exists", HttpStatus.CONFLICT);
        if (deptRepo.existsByName(name)) throw new AppException("Department name already exists", HttpStatus.CONFLICT);
        Department d = new Department();
        d.setName(name);
        d.setCode(code.toUpperCase());
        d.setDescription((String) req.get("description"));
        if (req.get("headFacultyId") != null) d.setHeadFacultyId(Long.parseLong(req.get("headFacultyId").toString()));
        d.setIsActive(true);
        return deptRepo.save(d);
    }

    @Transactional(readOnly = true)
    public List<Department> getAllDepartments() {
        return deptRepo.findByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public Department getDepartmentById(Long id) {
        return deptRepo.findById(id)
                .orElseThrow(() -> new AppException("Department not found: " + id, HttpStatus.NOT_FOUND));
    }

    public Department updateDepartment(Long id, Map<String, Object> req) {
        Department d = getDepartmentById(id);
        if (req.get("name") != null) d.setName((String) req.get("name"));
        if (req.get("description") != null) d.setDescription((String) req.get("description"));
        if (req.get("headFacultyId") != null)
            d.setHeadFacultyId(Long.parseLong(req.get("headFacultyId").toString()));
        return deptRepo.save(d);
    }

    // ── Course ────────────────────────────────────────────────────────────

    public Course createCourse(Map<String, Object> req) {
        String code = (String) req.get("code");
        if (courseRepo.existsByCode(code)) throw new AppException("Course code exists", HttpStatus.CONFLICT);
        Long deptId = Long.parseLong(req.get("departmentId").toString());
        if (!deptRepo.existsById(deptId)) throw new AppException("Department not found", HttpStatus.NOT_FOUND);
        Course c = new Course();
        c.setName((String) req.get("name"));
        c.setCode(code.toUpperCase());
        c.setDescription((String) req.get("description"));
        c.setDepartmentId(deptId);
        c.setTotalSemesters(Integer.parseInt(req.get("totalSemesters").toString()));
        if (req.get("durationYears") != null)
            c.setDurationYears(Integer.parseInt(req.get("durationYears").toString()));
        if (req.get("type") != null)
            c.setType(Course.CourseType.valueOf((String) req.get("type")));
        c.setIsActive(true);
        return courseRepo.save(c);
    }

    @Transactional(readOnly = true)
    public Course getCourseById(Long id) {
        return courseRepo.findById(id)
                .orElseThrow(() -> new AppException("Course not found: " + id, HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<Course> getCoursesByDept(Long deptId) {
        return courseRepo.findByDepartmentIdAndIsActiveTrue(deptId);
    }

    // ── Subject ───────────────────────────────────────────────────────────

    public Subject createSubject(Map<String, Object> req) {
        String code = (String) req.get("code");
        if (subjectRepo.existsByCode(code)) throw new AppException("Subject code exists", HttpStatus.CONFLICT);
        Subject s = new Subject();
        s.setName((String) req.get("name"));
        s.setCode(code.toUpperCase());
        s.setDescription((String) req.get("description"));
        s.setCourseId(Long.parseLong(req.get("courseId").toString()));
        s.setDepartmentId(Long.parseLong(req.get("departmentId").toString()));
        s.setSemester(Integer.parseInt(req.get("semester").toString()));
        s.setCredits(Integer.parseInt(req.get("credits").toString()));
        if (req.get("totalLectures") != null)
            s.setTotalLectures(Integer.parseInt(req.get("totalLectures").toString()));
        if (req.get("type") != null)
            s.setType(Subject.SubjectType.valueOf((String) req.get("type")));
        s.setIsActive(true);
        return subjectRepo.save(s);
    }

    @Transactional(readOnly = true)
    public Subject getSubjectById(Long id) {
        return subjectRepo.findById(id)
                .orElseThrow(() -> new AppException("Subject not found: " + id, HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<Subject> getSubjectsByCourse(Long courseId, Integer semester) {
        if (semester != null)
            return subjectRepo.findByCourseIdAndSemesterAndIsActiveTrue(courseId, semester);
        return subjectRepo.findByCourseIdAndIsActiveTrue(courseId);
    }

    // ── Batch ─────────────────────────────────────────────────────────────

    public Batch createBatch(Map<String, Object> req) {
        Batch b = new Batch();
        b.setName((String) req.get("name"));
        b.setCourseId(Long.parseLong(req.get("courseId").toString()));
        b.setDepartmentId(Long.parseLong(req.get("departmentId").toString()));
        b.setAcademicYear((String) req.get("academicYear"));
        if (req.get("currentSemester") != null)
            b.setCurrentSemester(Integer.parseInt(req.get("currentSemester").toString()));
        if (req.get("startDate") != null) b.setStartDate(LocalDate.parse((String) req.get("startDate")));
        if (req.get("endDate") != null)   b.setEndDate(LocalDate.parse((String) req.get("endDate")));
        if (req.get("maxStrength") != null)
            b.setMaxStrength(Integer.parseInt(req.get("maxStrength").toString()));
        b.setIsActive(true);
        return batchRepo.save(b);
    }

    @Transactional(readOnly = true)
    public Batch getBatchById(Long id) {
        return batchRepo.findById(id)
                .orElseThrow(() -> new AppException("Batch not found: " + id, HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<Batch> getBatchesByCourse(Long courseId) {
        return batchRepo.findByCourseIdAndIsActiveTrue(courseId);
    }

    @Transactional(readOnly = true)
    public List<Batch> getBatchesByDept(Long deptId) {
        return batchRepo.findByDepartmentIdAndIsActiveTrue(deptId);
    }

    public Batch updateBatchSemester(Long batchId, Integer semester) {
        Batch b = getBatchById(batchId);
        b.setCurrentSemester(semester);
        return batchRepo.save(b);
    }

    // ── Used by DataSeeder ─────────────────────────────────────────────────

    public boolean hasDepartments() { return deptRepo.count() > 0; }
    public DepartmentRepository getDeptRepo() { return deptRepo; }
    public CourseRepository getCourseRepo() { return courseRepo; }
    public SubjectRepository getSubjectRepo() { return subjectRepo; }
    public BatchRepository getBatchRepo() { return batchRepo; }
}
