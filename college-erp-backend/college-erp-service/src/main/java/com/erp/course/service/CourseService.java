package com.erp.course.service;

import com.erp.attendance.repository.AttendanceRepository;
import com.erp.auth.repository.UserRepository;
import com.erp.common.exception.AppException;
import com.erp.course.entity.*;
import com.erp.course.repository.*;
import com.erp.face.repository.FaceEnrollmentRepository;
import com.erp.faculty.entity.Faculty;
import com.erp.faculty.repository.FacultyAssignmentRepository;
import com.erp.faculty.repository.FacultyRepository;
import com.erp.marks.repository.MarksRepository;
import com.erp.student.entity.Student;
import com.erp.student.repository.StudentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Service
@Transactional
public class CourseService {

    private static final Logger log = Logger.getLogger(CourseService.class.getName());

    private final DepartmentRepository deptRepo;
    private final CourseRepository courseRepo;
    private final SubjectRepository subjectRepo;
    private final BatchRepository batchRepo;

    // Needed for cascading deletes
    private final StudentRepository studentRepo;
    private final FacultyRepository facultyRepo;
    private final FacultyAssignmentRepository facultyAssignmentRepo;
    private final AttendanceRepository attendanceRepo;
    private final MarksRepository marksRepo;
    private final FaceEnrollmentRepository faceEnrollmentRepo;
    private final UserRepository userRepo;

    public CourseService(DepartmentRepository deptRepo, CourseRepository courseRepo,
                         SubjectRepository subjectRepo, BatchRepository batchRepo,
                         StudentRepository studentRepo, FacultyRepository facultyRepo,
                         FacultyAssignmentRepository facultyAssignmentRepo,
                         AttendanceRepository attendanceRepo, MarksRepository marksRepo,
                         FaceEnrollmentRepository faceEnrollmentRepo, UserRepository userRepo) {
        this.deptRepo    = deptRepo;
        this.courseRepo  = courseRepo;
        this.subjectRepo = subjectRepo;
        this.batchRepo   = batchRepo;
        this.studentRepo = studentRepo;
        this.facultyRepo = facultyRepo;
        this.facultyAssignmentRepo = facultyAssignmentRepo;
        this.attendanceRepo = attendanceRepo;
        this.marksRepo = marksRepo;
        this.faceEnrollmentRepo = faceEnrollmentRepo;
        this.userRepo = userRepo;
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
    public List<Department> getAllDepartments(boolean includeInactive) {
        return includeInactive ? deptRepo.findAll() : deptRepo.findByIsActiveTrue();
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

    public Department setDepartmentActive(Long id, boolean active) {
        Department d = getDepartmentById(id);
        d.setIsActive(active);
        return deptRepo.save(d);
    }

    /**
     * PERMANENTLY delete a department and everything under it:
     * courses -> subjects -> batches -> students (+ their attendance/marks/
     * face data + login) and faculty (+ their assignments + login).
     */
    public void deleteDepartmentCascade(Long id) {
        Department d = getDepartmentById(id);

        for (Course c : courseRepo.findByDepartmentId(id)) {
            deleteCourseCascade(c.getId());
        }
        for (Batch b : batchRepo.findByDepartmentId(id)) {
            deleteBatchCascade(b.getId());
        }
        for (Student s : studentRepo.findByDepartmentId(id)) {
            hardDeleteStudentInternal(s);
        }
        for (Faculty f : facultyRepo.findByDepartmentId(id)) {
            hardDeleteFacultyInternal(f);
        }

        deptRepo.delete(d);
        log.info("Department permanently deleted with all dependent data: " + id);
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
    public List<Course> getCoursesByDept(Long deptId, boolean includeInactive) {
        return includeInactive ? courseRepo.findByDepartmentId(deptId) : courseRepo.findByDepartmentIdAndIsActiveTrue(deptId);
    }

    public Course setCourseActive(Long id, boolean active) {
        Course c = getCourseById(id);
        c.setIsActive(active);
        return courseRepo.save(c);
    }

    /** PERMANENTLY delete a course: its subjects, its batches (+ students in them). */
    public void deleteCourseCascade(Long id) {
        Course c = getCourseById(id);

        for (Subject s : subjectRepo.findByCourseId(id)) {
            deleteSubjectCascade(s.getId());
        }
        for (Batch b : batchRepo.findByCourseId(id)) {
            deleteBatchCascade(b.getId());
        }

        courseRepo.delete(c);
        log.info("Course permanently deleted with subjects/batches: " + id);
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
        s.setIsActive(true);
        return subjectRepo.save(s);
    }

    @Transactional(readOnly = true)
    public Subject getSubjectById(Long id) {
        return subjectRepo.findById(id)
                .orElseThrow(() -> new AppException("Subject not found: " + id, HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<Subject> getSubjectsByCourse(Long courseId, Integer semester, boolean includeInactive) {
        if (includeInactive) return subjectRepo.findByCourseId(courseId);
        if (semester != null)
            return subjectRepo.findByCourseIdAndSemesterAndIsActiveTrue(courseId, semester);
        return subjectRepo.findByCourseIdAndIsActiveTrue(courseId);
    }

    public Subject setSubjectActive(Long id, boolean active) {
        Subject s = getSubjectById(id);
        s.setIsActive(active);
        return subjectRepo.save(s);
    }

    /** PERMANENTLY delete a subject: its attendance + marks history + faculty assignments referencing it. */
    public void deleteSubjectCascade(Long id) {
        Subject s = getSubjectById(id);

        attendanceRepo.deleteBySubjectId(id);
        marksRepo.deleteBySubjectId(id);
        facultyAssignmentRepo.findBySubjectIdAndIsActiveTrue(id)
                .forEach(facultyAssignmentRepo::delete);

        subjectRepo.delete(s);
        log.info("Subject permanently deleted with attendance/marks history and assignments: " + id);
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
    public List<Batch> getBatchesByCourse(Long courseId, boolean includeInactive) {
        return includeInactive ? batchRepo.findByCourseId(courseId) : batchRepo.findByCourseIdAndIsActiveTrue(courseId);
    }

    @Transactional(readOnly = true)
    public List<Batch> getBatchesByDept(Long deptId, boolean includeInactive) {
        return includeInactive ? batchRepo.findByDepartmentId(deptId) : batchRepo.findByDepartmentIdAndIsActiveTrue(deptId);
    }

    public Batch updateBatchSemester(Long batchId, Integer semester) {
        Batch b = getBatchById(batchId);
        b.setCurrentSemester(semester);
        return batchRepo.save(b);
    }

    public Batch setBatchActive(Long id, boolean active) {
        Batch b = getBatchById(id);
        b.setIsActive(active);
        return batchRepo.save(b);
    }

    /** PERMANENTLY delete a batch and every student enrolled in it (with their dependent data). */
    public void deleteBatchCascade(Long id) {
        Batch b = getBatchById(id);

        List<Student> students = studentRepo.findByBatchId(id);
        for (Student s : students) {
            hardDeleteStudentInternal(s);
        }

        batchRepo.delete(b);
        log.info("Batch permanently deleted with " + students.size() + " student(s): " + id);
    }

    // ── Internal cascade helpers ─────────────────────────────────────────

    private void hardDeleteStudentInternal(Student s) {
        attendanceRepo.deleteByStudentId(s.getId());
        marksRepo.deleteByStudentId(s.getId());
        faceEnrollmentRepo.deleteByStudentId(s.getId());
        if (s.getUserId() != null) userRepo.findById(s.getUserId()).ifPresent(userRepo::delete);
        studentRepo.delete(s);
    }

    private void hardDeleteFacultyInternal(Faculty f) {
        facultyAssignmentRepo.findByFacultyIdAndIsActiveTrue(f.getId()).forEach(facultyAssignmentRepo::delete);
        if (f.getUserId() != null) userRepo.findById(f.getUserId()).ifPresent(userRepo::delete);
        facultyRepo.delete(f);
    }

    // ── Used by DataSeeder ─────────────────────────────────────────────────

    public boolean hasDepartments() { return deptRepo.count() > 0; }
    public DepartmentRepository getDeptRepo() { return deptRepo; }
    public CourseRepository getCourseRepo() { return courseRepo; }
    public SubjectRepository getSubjectRepo() { return subjectRepo; }
    public BatchRepository getBatchRepo() { return batchRepo; }
}
