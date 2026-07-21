package com.erp.common.config;

import com.erp.auth.entity.User;
import com.erp.auth.repository.UserRepository;
import com.erp.course.entity.*;
import com.erp.course.repository.*;
import com.erp.faculty.dto.FacultyDto;
import com.erp.faculty.service.FacultyService;
import com.erp.student.dto.StudentDto;
import com.erp.student.entity.Student;
import com.erp.student.service.StudentService;
import com.erp.parent.service.ParentService;
import com.erp.parent.entity.Parent;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Seeds demo data on first startup only (skipped if departments already exist).
 *
 * IMPORTANT: student1 / faculty1 / parent1 are created through the same
 * service classes used by the admin UI (StudentService / FacultyService /
 * ParentService), so they show up correctly in every list, dashboard count,
 * and search — exactly like any admin-created record. This was previously
 * a bug where these three demo accounts were bare login rows with no
 * underlying Student/Faculty/Parent record, so they were invisible everywhere
 * except the login screen.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = Logger.getLogger(DataSeeder.class.getName());
    private static final String DEFAULT_PWD = "Password@123";

    private final UserRepository       userRepo;
    private final DepartmentRepository deptRepo;
    private final CourseRepository     courseRepo;
    private final SubjectRepository    subjectRepo;
    private final BatchRepository      batchRepo;
    private final PasswordEncoder      encoder;
    private final StudentService       studentService;
    private final FacultyService       facultyService;
    private final ParentService        parentService;

    public DataSeeder(UserRepository userRepo,
                      DepartmentRepository deptRepo,
                      CourseRepository courseRepo,
                      SubjectRepository subjectRepo,
                      BatchRepository batchRepo,
                      PasswordEncoder encoder,
                      StudentService studentService,
                      FacultyService facultyService,
                      ParentService parentService) {
        this.userRepo    = userRepo;
        this.deptRepo    = deptRepo;
        this.courseRepo  = courseRepo;
        this.subjectRepo = subjectRepo;
        this.batchRepo   = batchRepo;
        this.encoder     = encoder;
        this.studentService = studentService;
        this.facultyService = facultyService;
        this.parentService  = parentService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (deptRepo.count() > 0) {
            log.info("Seed data already present — skipping DataSeeder.");
            return;
        }

        Map<String, Object> seeded = seedCourseData();
        seedAdmin();
        seedDemoAccounts(seeded);
    }

    // ── Admin (no linked profile record — just a login) ──────────────────

    private void seedAdmin() {
        User admin = new User();
        admin.setUsername("admin");
        admin.setName("System Administrator");
        admin.setEmail("admin@college.edu");
        admin.setPassword(encoder.encode(DEFAULT_PWD));
        admin.setRole(User.Role.ADMIN);
        admin.setIsActive(true);
        admin.setIsEmailVerified(true);
        admin.setMustChangePassword(false);
        userRepo.save(admin);
        log.info("Admin seeded. Login: admin / Password@123");
    }

    // ── Demo student/faculty/parent — created via real service layer ────

    private void seedDemoAccounts(Map<String, Object> seeded) {
        Long csDeptId    = (Long) seeded.get("csDeptId");
        Long btechCsId   = (Long) seeded.get("btechCsId");
        Long batchCsAId  = (Long) seeded.get("batchCsA2026Id");

        // Faculty
        FacultyDto.CreateRequest facReq = new FacultyDto.CreateRequest();
        facReq.setFirstName("Faculty"); facReq.setLastName("One");
        facReq.setEmail("faculty1@college.edu"); facReq.setPhone("9000000001");
        facReq.setDepartmentId(csDeptId); facReq.setDesignation("Assistant Professor");
        facReq.setQualification("M.Tech"); facReq.setExperienceYears(5);
        facultyService.create(facReq);

        // Parent (create first so we can link the student to it)
        Map<String, Object> parentReq = new HashMap<>();
        parentReq.put("firstName", "Parent"); parentReq.put("lastName", "One");
        parentReq.put("email", "parent1@gmail.com"); parentReq.put("phone", "9000000099");
        parentReq.put("relationToStudent", "FATHER");
        Parent parent = parentService.create(parentReq);

        // Student — linked to the demo parent and real seeded batch/course
        StudentDto.CreateRequest stuReq = new StudentDto.CreateRequest();
        stuReq.setFirstName("Student"); stuReq.setLastName("One");
        stuReq.setEmail("student1@college.edu"); stuReq.setPhone("9000000002");
        stuReq.setDepartmentId(csDeptId); stuReq.setCourseId(btechCsId);
        stuReq.setBatchId(batchCsAId); stuReq.setCurrentSemester(1);
        stuReq.setAdmissionYear(2026); stuReq.setParentId(parent.getId());
        studentService.createStudent(stuReq);

        log.info("Demo accounts seeded via service layer: faculty1, parent1, student1 (all Password@123, " +
                "visible in admin lists and dashboard like any real record).");
    }

    // ── Course data ───────────────────────────────────────────────────────

    private Map<String, Object> seedCourseData() {
        // ── Departments ────────────────────────────────────────────────────
        Department cs   = dept("Computer Science",             "CS",   "Dept of Computer Science & Engineering");
        Department it   = dept("Information Technology",       "IT",   "Dept of Information Technology");
        Department ec   = dept("Electronics & Communication",  "EC",   "Dept of Electronics & Communication");
        Department mech = dept("Mechanical Engineering",       "MECH", "Dept of Mechanical Engineering");

        // ── Courses ────────────────────────────────────────────────────────
        Course btechCs   = course("B.Tech Computer Science",     "BTECH-CS",   cs.getId(),   8, 4, Course.CourseType.UNDERGRADUATE);
        Course btechIt   = course("B.Tech Information Technology","BTECH-IT",  it.getId(),   8, 4, Course.CourseType.UNDERGRADUATE);
        Course mtechCs   = course("M.Tech Computer Science",     "MTECH-CS",   cs.getId(),   4, 2, Course.CourseType.POSTGRADUATE);
        Course btechMech = course("B.Tech Mechanical Engg",      "BTECH-MECH", mech.getId(), 8, 4, Course.CourseType.UNDERGRADUATE);

        // ── Subjects — B.Tech CS Sem 1 ─────────────────────────────────────
        subject("Engineering Mathematics I",    "MATH101", btechCs.getId(), cs.getId(), 1, 4, 60);
        subject("Programming in C",             "CS101",   btechCs.getId(), cs.getId(), 1, 4, 60);
        subject("Programming Lab",              "CS101L",  btechCs.getId(), cs.getId(), 1, 2, 30);
        subject("Engineering Physics",          "PHY101",  btechCs.getId(), cs.getId(), 1, 3, 45);
        subject("Communication Skills",         "ENG101",  btechCs.getId(), cs.getId(), 1, 2, 30);

        // ── Subjects — B.Tech CS Sem 2 ─────────────────────────────────────
        subject("Engineering Mathematics II",   "MATH201", btechCs.getId(), cs.getId(), 2, 4, 60);
        subject("Data Structures",              "CS201",   btechCs.getId(), cs.getId(), 2, 4, 60);
        subject("Data Structures Lab",          "CS201L",  btechCs.getId(), cs.getId(), 2, 2, 30);
        subject("Digital Electronics",          "EC201",   btechCs.getId(), cs.getId(), 2, 3, 45);

        // ── Subjects — B.Tech CS Sem 3 ─────────────────────────────────────
        subject("Object Oriented Programming",  "CS301",   btechCs.getId(), cs.getId(), 3, 4, 60);
        subject("Database Management Systems",  "CS302",   btechCs.getId(), cs.getId(), 3, 4, 60);
        subject("Operating Systems",            "CS303",   btechCs.getId(), cs.getId(), 3, 4, 60);
        subject("DBMS Lab",                     "CS302L",  btechCs.getId(), cs.getId(), 3, 2, 30);

        // ── Subjects — B.Tech CS Sem 4 ─────────────────────────────────────
        subject("Computer Networks",            "CS401",   btechCs.getId(), cs.getId(), 4, 4, 60);
        subject("Software Engineering",         "CS402",   btechCs.getId(), cs.getId(), 4, 4, 60);
        subject("Theory of Computation",        "CS403",   btechCs.getId(), cs.getId(), 4, 4, 60);
        subject("Networks Lab",                 "CS401L",  btechCs.getId(), cs.getId(), 4, 2, 30);

        // ── Subjects — B.Tech CS Sem 5 ─────────────────────────────────────
        subject("Artificial Intelligence",      "CS501",   btechCs.getId(), cs.getId(), 5, 4, 60);
        subject("Machine Learning",             "CS502",   btechCs.getId(), cs.getId(), 5, 4, 60);
        subject("Web Technologies",             "CS503",   btechCs.getId(), cs.getId(), 5, 3, 45);
        subject("AI Lab",                       "CS501L",  btechCs.getId(), cs.getId(), 5, 2, 30);

        // ── Subjects — B.Tech CS Sem 6 ─────────────────────────────────────
        subject("Cloud Computing",              "CS601",   btechCs.getId(), cs.getId(), 6, 4, 60);
        subject("Information Security",         "CS602",   btechCs.getId(), cs.getId(), 6, 4, 60);
        subject("Mobile Application Development","CS603",  btechCs.getId(), cs.getId(), 6, 3, 45);

        // ── Batches — with 2026 as current year ───────────────────────────

        Batch csA2026 = batch("CS-A-2026", btechCs.getId(), cs.getId(), "2026-27", 1,
              LocalDate.of(2026, 7, 1), LocalDate.of(2030, 5, 31), 60);
        batch("CS-B-2026", btechCs.getId(), cs.getId(), "2026-27", 1,
              LocalDate.of(2026, 7, 1), LocalDate.of(2030, 5, 31), 60);
        batch("IT-A-2026", btechIt.getId(), it.getId(), "2026-27", 1,
              LocalDate.of(2026, 7, 1), LocalDate.of(2030, 5, 31), 60);

        batch("CS-A-2025", btechCs.getId(), cs.getId(), "2025-26", 3,
              LocalDate.of(2025, 7, 1), LocalDate.of(2029, 5, 31), 60);
        batch("CS-B-2025", btechCs.getId(), cs.getId(), "2025-26", 3,
              LocalDate.of(2025, 7, 1), LocalDate.of(2029, 5, 31), 60);
        batch("IT-A-2025", btechIt.getId(), it.getId(), "2025-26", 3,
              LocalDate.of(2025, 7, 1), LocalDate.of(2029, 5, 31), 60);

        batch("CS-A-2024", btechCs.getId(), cs.getId(), "2024-25", 5,
              LocalDate.of(2024, 7, 1), LocalDate.of(2028, 5, 31), 60);
        batch("IT-A-2024", btechIt.getId(), it.getId(), "2024-25", 5,
              LocalDate.of(2024, 7, 1), LocalDate.of(2028, 5, 31), 60);

        batch("CS-A-2023", btechCs.getId(), cs.getId(), "2023-24", 7,
              LocalDate.of(2023, 7, 1), LocalDate.of(2027, 5, 31), 60);

        batch("MTECH-CS-2026", mtechCs.getId(), cs.getId(), "2026-27", 1,
              LocalDate.of(2026, 7, 1), LocalDate.of(2028, 5, 31), 30);

        log.info("Seed data complete: " + deptRepo.count() + " depts, " +
                courseRepo.count() + " courses, " + subjectRepo.count() +
                " subjects, " + batchRepo.count() + " batches.");

        Map<String, Object> result = new HashMap<>();
        result.put("csDeptId", cs.getId());
        result.put("btechCsId", btechCs.getId());
        result.put("batchCsA2026Id", csA2026.getId());
        return result;
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private Department dept(String name, String code, String desc) {
        Department d = new Department();
        d.setName(name); d.setCode(code); d.setDescription(desc); d.setIsActive(true);
        return deptRepo.save(d);
    }

    private Course course(String name, String code, Long deptId,
                          int sems, int years, Course.CourseType type) {
        Course c = new Course();
        c.setName(name); c.setCode(code); c.setDepartmentId(deptId);
        c.setTotalSemesters(sems); c.setDurationYears(years); c.setType(type); c.setIsActive(true);
        return courseRepo.save(c);
    }

    private void subject(String name, String code, Long courseId, Long deptId,
                         int semester, int credits, int lectures) {
        Subject s = new Subject();
        s.setName(name); s.setCode(code); s.setCourseId(courseId); s.setDepartmentId(deptId);
        s.setSemester(semester); s.setCredits(credits); s.setTotalLectures(lectures);
        s.setIsActive(true);
        subjectRepo.save(s);
    }

    private Batch batch(String name, Long courseId, Long deptId, String academicYear,
                       int currentSemester, LocalDate start, LocalDate end, int maxStrength) {
        Batch b = new Batch();
        b.setName(name); b.setCourseId(courseId); b.setDepartmentId(deptId);
        b.setAcademicYear(academicYear); b.setCurrentSemester(currentSemester);
        b.setStartDate(start); b.setEndDate(end); b.setMaxStrength(maxStrength); b.setIsActive(true);
        return batchRepo.save(b);
    }
}
