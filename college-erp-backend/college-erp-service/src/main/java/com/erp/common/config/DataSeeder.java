package com.erp.common.config;

import com.erp.auth.entity.User;
import com.erp.auth.repository.UserRepository;
import com.erp.course.entity.*;
import com.erp.course.repository.*;
import com.erp.faculty.entity.Faculty;
import com.erp.faculty.repository.FacultyRepository;
import com.erp.parent.entity.Parent;
import com.erp.parent.repository.ParentRepository;
import com.erp.student.entity.Student;
import com.erp.student.repository.StudentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.logging.Logger;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = Logger.getLogger(DataSeeder.class.getName());
    private static final String DEFAULT_PWD = "Password@123";

    private final UserRepository       userRepo;
    private final DepartmentRepository deptRepo;
    private final CourseRepository     courseRepo;
    private final SubjectRepository    subjectRepo;
    private final BatchRepository      batchRepo;
    private final StudentRepository    studentRepo;
    private final FacultyRepository    facultyRepo;
    private final ParentRepository     parentRepo;
    private final PasswordEncoder      encoder;

    public DataSeeder(UserRepository userRepo,
                      DepartmentRepository deptRepo,
                      CourseRepository courseRepo,
                      SubjectRepository subjectRepo,
                      BatchRepository batchRepo,
                      StudentRepository studentRepo,
                      FacultyRepository facultyRepo,
                      ParentRepository parentRepo,
                      PasswordEncoder encoder) {
        this.userRepo    = userRepo;
        this.deptRepo    = deptRepo;
        this.courseRepo  = courseRepo;
        this.subjectRepo = subjectRepo;
        this.batchRepo   = batchRepo;
        this.studentRepo = studentRepo;
        this.facultyRepo = facultyRepo;
        this.parentRepo  = parentRepo;
        this.encoder     = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Always ensure the admin account exists, regardless of other seed state
        seedAdmin();

        if (deptRepo.count() > 0) {
            log.info("Course data already seeded — skipping.");
            return;
        }

        try {
            seedAll();
        } catch (Exception e) {
            log.severe("DataSeeder failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void seedAdmin() {
        if (userRepo.existsByUsername("admin")) {
            log.info("Admin user already exists — skipping admin seed.");
            return;
        }
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
        log.info("=== Admin seeded. Login: admin / Password@123 ===");
    }

    private void seedAll() {
        // ── Departments ──────────────────────────────────────────────────
        Department cs   = dept("Computer Science",            "CS",   "Dept of Computer Science & Engineering");
        Department it   = dept("Information Technology",      "IT",   "Dept of Information Technology");
        Department ec   = dept("Electronics & Communication", "EC",   "Dept of Electronics & Communication");
        Department mech = dept("Mechanical Engineering",      "MECH", "Dept of Mechanical Engineering");

        // ── Courses ──────────────────────────────────────────────────────
        Course btechCs   = course("B.Tech Computer Science",      "BTECH-CS",   cs.getId(),   8, 4, Course.CourseType.UNDERGRADUATE);
        Course btechIt   = course("B.Tech Information Technology","BTECH-IT",   it.getId(),   8, 4, Course.CourseType.UNDERGRADUATE);
        Course mtechCs   = course("M.Tech Computer Science",      "MTECH-CS",   cs.getId(),   4, 2, Course.CourseType.POSTGRADUATE);
        Course btechMech = course("B.Tech Mechanical Engg",       "BTECH-MECH", mech.getId(), 8, 4, Course.CourseType.UNDERGRADUATE);

        // ── Subjects — B.Tech CS ─────────────────────────────────────────
        subject("Engineering Mathematics I",     "MATH101", btechCs.getId(), cs.getId(), 1, 4, 60);
        subject("Programming in C",              "CS101",   btechCs.getId(), cs.getId(), 1, 4, 60);
        subject("Programming Lab",               "CS101L",  btechCs.getId(), cs.getId(), 1, 2, 30);
        subject("Engineering Physics",           "PHY101",  btechCs.getId(), cs.getId(), 1, 3, 45);
        subject("Communication Skills",          "ENG101",  btechCs.getId(), cs.getId(), 1, 2, 30);
        subject("Engineering Mathematics II",    "MATH201", btechCs.getId(), cs.getId(), 2, 4, 60);
        subject("Data Structures",               "CS201",   btechCs.getId(), cs.getId(), 2, 4, 60);
        subject("Data Structures Lab",           "CS201L",  btechCs.getId(), cs.getId(), 2, 2, 30);
        subject("Digital Electronics",           "EC201",   btechCs.getId(), cs.getId(), 2, 3, 45);
        subject("Object Oriented Programming",   "CS301",   btechCs.getId(), cs.getId(), 3, 4, 60);
        subject("Database Management Systems",   "CS302",   btechCs.getId(), cs.getId(), 3, 4, 60);
        subject("Operating Systems",             "CS303",   btechCs.getId(), cs.getId(), 3, 4, 60);
        subject("DBMS Lab",                      "CS302L",  btechCs.getId(), cs.getId(), 3, 2, 30);
        subject("Computer Networks",             "CS401",   btechCs.getId(), cs.getId(), 4, 4, 60);
        subject("Software Engineering",          "CS402",   btechCs.getId(), cs.getId(), 4, 4, 60);
        subject("Theory of Computation",         "CS403",   btechCs.getId(), cs.getId(), 4, 4, 60);
        subject("Networks Lab",                  "CS401L",  btechCs.getId(), cs.getId(), 4, 2, 30);
        subject("Artificial Intelligence",       "CS501",   btechCs.getId(), cs.getId(), 5, 4, 60);
        subject("Machine Learning",              "CS502",   btechCs.getId(), cs.getId(), 5, 4, 60);
        subject("Web Technologies",              "CS503",   btechCs.getId(), cs.getId(), 5, 3, 45);
        subject("AI Lab",                        "CS501L",  btechCs.getId(), cs.getId(), 5, 2, 30);
        subject("Cloud Computing",               "CS601",   btechCs.getId(), cs.getId(), 6, 4, 60);
        subject("Information Security",          "CS602",   btechCs.getId(), cs.getId(), 6, 4, 60);
        subject("Mobile Application Development","CS603",   btechCs.getId(), cs.getId(), 6, 3, 45);

        // ── Batches ──────────────────────────────────────────────────────
        Batch csA2026 = batch("CS-A-2026", btechCs.getId(), cs.getId(), "2026-27", 1,
                LocalDate.of(2026, 7, 1), LocalDate.of(2030, 5, 31), 60);
        batch("CS-B-2026", btechCs.getId(), cs.getId(), "2026-27", 1,
                LocalDate.of(2026, 7, 1), LocalDate.of(2030, 5, 31), 60);
        batch("IT-A-2026", btechIt.getId(), it.getId(), "2026-27", 1,
                LocalDate.of(2026, 7, 1), LocalDate.of(2030, 5, 31), 60);
        batch("CS-A-2025", btechCs.getId(), cs.getId(), "2025-26", 3,
                LocalDate.of(2025, 7, 1), LocalDate.of(2029, 5, 31), 60);
        batch("CS-A-2024", btechCs.getId(), cs.getId(), "2024-25", 5,
                LocalDate.of(2024, 7, 1), LocalDate.of(2028, 5, 31), 60);
        batch("CS-A-2023", btechCs.getId(), cs.getId(), "2023-24", 7,
                LocalDate.of(2023, 7, 1), LocalDate.of(2027, 5, 31), 60);
        batch("MTECH-CS-2026", mtechCs.getId(), cs.getId(), "2026-27", 1,
                LocalDate.of(2026, 7, 1), LocalDate.of(2028, 5, 31), 30);

        // ── Demo Faculty ──────────────────────────────────────────────────
        Faculty fac = new Faculty();
        fac.setEmployeeId("FAC010001");
        fac.setFirstName("Faculty"); fac.setLastName("One");
        fac.setEmail("faculty1@college.edu"); fac.setPhone("9000000001");
        fac.setDepartmentId(cs.getId()); fac.setDesignation("Assistant Professor");
        fac.setQualification("M.Tech"); fac.setExperienceYears(5);
        fac.setStatus(Faculty.FacultyStatus.ACTIVE);
        fac = facultyRepo.save(fac);

        User facUser = new User();
        facUser.setUsername("faculty1");
        facUser.setName("Faculty One");
        facUser.setEmail("faculty1@college.edu");
        facUser.setPassword(encoder.encode(DEFAULT_PWD));
        facUser.setRole(User.Role.FACULTY);
        facUser.setReferenceId(fac.getId());
        facUser.setIsActive(true);
        facUser.setIsEmailVerified(true);
        facUser.setMustChangePassword(true);
        facUser = userRepo.save(facUser);
        fac.setUserId(facUser.getId());
        facultyRepo.save(fac);

        // ── Demo Parent ───────────────────────────────────────────────────
        Parent par = new Parent();
        par.setFirstName("Parent"); par.setLastName("One");
        par.setEmail("parent1@gmail.com"); par.setPhone("9000000099");
        par.setRelationToStudent("FATHER");
        par.setIsActive(true);
        par = parentRepo.save(par);

        User parUser = new User();
        parUser.setUsername("parent1");
        parUser.setName("Parent One");
        parUser.setEmail("parent1@gmail.com");
        parUser.setPassword(encoder.encode(DEFAULT_PWD));
        parUser.setRole(User.Role.PARENT);
        parUser.setReferenceId(par.getId());
        parUser.setIsActive(true);
        parUser.setIsEmailVerified(true);
        parUser.setMustChangePassword(true);
        parUser = userRepo.save(parUser);
        par.setUserId(parUser.getId());
        parentRepo.save(par);

        // ── Demo Student ──────────────────────────────────────────────────
        long count = studentRepo.count() + 1;
        String enrollmentNo = String.format("ENR%d%02d%04d", 26, cs.getId(), count);

        Student stu = new Student();
        stu.setEnrollmentNumber(enrollmentNo);
        stu.setFirstName("Student"); stu.setLastName("One");
        stu.setEmail("student1@college.edu"); stu.setPhone("9000000002");
        stu.setDepartmentId(cs.getId());
        stu.setCourseId(btechCs.getId());
        stu.setBatchId(csA2026.getId());
        stu.setCurrentSemester(1);
        stu.setAdmissionYear(2026);
        stu.setParentId(par.getId());
        stu.setStatus(Student.StudentStatus.ACTIVE);
        stu.setFaceEnrolled(false);
        stu = studentRepo.save(stu);

        User stuUser = new User();
        stuUser.setUsername(enrollmentNo);
        stuUser.setName("Student One");
        stuUser.setEmail("student1@college.edu");
        stuUser.setPassword(encoder.encode(DEFAULT_PWD));
        stuUser.setRole(User.Role.STUDENT);
        stuUser.setReferenceId(stu.getId());
        stuUser.setIsActive(true);
        stuUser.setIsEmailVerified(true);
        stuUser.setMustChangePassword(true);
        stuUser = userRepo.save(stuUser);
        stu.setUserId(stuUser.getId());
        studentRepo.save(stu);

        log.info("=== Seed complete: " + deptRepo.count() + " depts, " +
                courseRepo.count() + " courses, " + subjectRepo.count() +
                " subjects, " + batchRepo.count() + " batches. ===");
        log.info("=== Demo logins (all Password@123): admin, faculty1, parent1, " + enrollmentNo + " ===");
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private Department dept(String name, String code, String desc) {
        Department d = new Department();
        d.setName(name); d.setCode(code); d.setDescription(desc); d.setIsActive(true);
        return deptRepo.save(d);
    }

    private Course course(String name, String code, Long deptId, int sems, int years, Course.CourseType type) {
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
