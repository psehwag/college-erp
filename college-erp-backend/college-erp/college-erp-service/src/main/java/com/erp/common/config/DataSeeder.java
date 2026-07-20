package com.erp.common.config;

import com.erp.auth.entity.User;
import com.erp.auth.repository.UserRepository;
import com.erp.course.entity.*;
import com.erp.course.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    private final PasswordEncoder      encoder;

    public DataSeeder(UserRepository userRepo,
                      DepartmentRepository deptRepo,
                      CourseRepository courseRepo,
                      SubjectRepository subjectRepo,
                      BatchRepository batchRepo,
                      PasswordEncoder encoder) {
        this.userRepo    = userRepo;
        this.deptRepo    = deptRepo;
        this.courseRepo  = courseRepo;
        this.subjectRepo = subjectRepo;
        this.batchRepo   = batchRepo;
        this.encoder     = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedUsers();
        seedCourseData();
    }

    // ── Users ─────────────────────────────────────────────────────────────

    private void seedUsers() {
        if (userRepo.count() > 0) {
            log.info("Users already seeded — skipping.");
            return;
        }

        // Admin
        createUser("admin", "admin@college.edu", DEFAULT_PWD, User.Role.ADMIN, null, false);
        // Sample faculty
        createUser("faculty1", "faculty1@college.edu", DEFAULT_PWD, User.Role.FACULTY, null, true);
        createUser("faculty2", "faculty2@college.edu", DEFAULT_PWD, User.Role.FACULTY, null, true);
        // Sample student
        createUser("student1", "student1@college.edu", DEFAULT_PWD, User.Role.STUDENT, null, true);
        // Sample parent
        createUser("parent1", "parent1@gmail.com", DEFAULT_PWD, User.Role.PARENT, null, true);

        log.info("Default users seeded. Login: admin / Password@123");
    }

    private void createUser(String username, String email, String password,
                             User.Role role, Long referenceId, boolean mustChange) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword(encoder.encode(password));
        u.setRole(role);
        u.setReferenceId(referenceId);
        u.setIsActive(true);
        u.setIsEmailVerified(true);
        u.setMustChangePassword(mustChange);
        userRepo.save(u);
    }

    // ── Course data ───────────────────────────────────────────────────────

    private void seedCourseData() {
        if (deptRepo.count() > 0) {
            log.info("Course data already seeded — skipping.");
            return;
        }

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
        subject("Engineering Mathematics I",    "MATH101", btechCs.getId(), cs.getId(), 1, 4, 60, Subject.SubjectType.THEORY);
        subject("Programming in C",             "CS101",   btechCs.getId(), cs.getId(), 1, 4, 60, Subject.SubjectType.THEORY);
        subject("Programming Lab",              "CS101L",  btechCs.getId(), cs.getId(), 1, 2, 30, Subject.SubjectType.PRACTICAL);
        subject("Engineering Physics",          "PHY101",  btechCs.getId(), cs.getId(), 1, 3, 45, Subject.SubjectType.THEORY);
        subject("Communication Skills",         "ENG101",  btechCs.getId(), cs.getId(), 1, 2, 30, Subject.SubjectType.THEORY);

        // ── Subjects — B.Tech CS Sem 2 ─────────────────────────────────────
        subject("Engineering Mathematics II",   "MATH201", btechCs.getId(), cs.getId(), 2, 4, 60, Subject.SubjectType.THEORY);
        subject("Data Structures",              "CS201",   btechCs.getId(), cs.getId(), 2, 4, 60, Subject.SubjectType.THEORY);
        subject("Data Structures Lab",          "CS201L",  btechCs.getId(), cs.getId(), 2, 2, 30, Subject.SubjectType.PRACTICAL);
        subject("Digital Electronics",          "EC201",   btechCs.getId(), cs.getId(), 2, 3, 45, Subject.SubjectType.THEORY);

        // ── Subjects — B.Tech CS Sem 3 ─────────────────────────────────────
        subject("Object Oriented Programming",  "CS301",   btechCs.getId(), cs.getId(), 3, 4, 60, Subject.SubjectType.THEORY);
        subject("Database Management Systems",  "CS302",   btechCs.getId(), cs.getId(), 3, 4, 60, Subject.SubjectType.THEORY);
        subject("Operating Systems",            "CS303",   btechCs.getId(), cs.getId(), 3, 4, 60, Subject.SubjectType.THEORY);
        subject("DBMS Lab",                     "CS302L",  btechCs.getId(), cs.getId(), 3, 2, 30, Subject.SubjectType.PRACTICAL);

        // ── Subjects — B.Tech CS Sem 4 ─────────────────────────────────────
        subject("Computer Networks",            "CS401",   btechCs.getId(), cs.getId(), 4, 4, 60, Subject.SubjectType.THEORY);
        subject("Software Engineering",         "CS402",   btechCs.getId(), cs.getId(), 4, 4, 60, Subject.SubjectType.THEORY);
        subject("Theory of Computation",        "CS403",   btechCs.getId(), cs.getId(), 4, 4, 60, Subject.SubjectType.THEORY);
        subject("Networks Lab",                 "CS401L",  btechCs.getId(), cs.getId(), 4, 2, 30, Subject.SubjectType.PRACTICAL);

        // ── Subjects — B.Tech CS Sem 5 ─────────────────────────────────────
        subject("Artificial Intelligence",      "CS501",   btechCs.getId(), cs.getId(), 5, 4, 60, Subject.SubjectType.THEORY);
        subject("Machine Learning",             "CS502",   btechCs.getId(), cs.getId(), 5, 4, 60, Subject.SubjectType.THEORY);
        subject("Web Technologies",             "CS503",   btechCs.getId(), cs.getId(), 5, 3, 45, Subject.SubjectType.THEORY);
        subject("AI Lab",                       "CS501L",  btechCs.getId(), cs.getId(), 5, 2, 30, Subject.SubjectType.PRACTICAL);

        // ── Subjects — B.Tech CS Sem 6 ─────────────────────────────────────
        subject("Cloud Computing",              "CS601",   btechCs.getId(), cs.getId(), 6, 4, 60, Subject.SubjectType.THEORY);
        subject("Information Security",         "CS602",   btechCs.getId(), cs.getId(), 6, 4, 60, Subject.SubjectType.THEORY);
        subject("Mobile Application Development","CS603",  btechCs.getId(), cs.getId(), 6, 3, 45, Subject.SubjectType.THEORY);

        // ── Batches — with 2026 as current year ───────────────────────────

        // 2026 intake — Semester 1 (just joined)
        batch("CS-A-2026", btechCs.getId(), cs.getId(), "2026-27", 1,
              LocalDate.of(2026, 7, 1), LocalDate.of(2030, 5, 31), 60);
        batch("CS-B-2026", btechCs.getId(), cs.getId(), "2026-27", 1,
              LocalDate.of(2026, 7, 1), LocalDate.of(2030, 5, 31), 60);
        batch("IT-A-2026", btechIt.getId(), it.getId(), "2026-27", 1,
              LocalDate.of(2026, 7, 1), LocalDate.of(2030, 5, 31), 60);

        // 2025 intake — Semester 3 (second year)
        batch("CS-A-2025", btechCs.getId(), cs.getId(), "2025-26", 3,
              LocalDate.of(2025, 7, 1), LocalDate.of(2029, 5, 31), 60);
        batch("CS-B-2025", btechCs.getId(), cs.getId(), "2025-26", 3,
              LocalDate.of(2025, 7, 1), LocalDate.of(2029, 5, 31), 60);
        batch("IT-A-2025", btechIt.getId(), it.getId(), "2025-26", 3,
              LocalDate.of(2025, 7, 1), LocalDate.of(2029, 5, 31), 60);

        // 2024 intake — Semester 5 (third year)
        batch("CS-A-2024", btechCs.getId(), cs.getId(), "2024-25", 5,
              LocalDate.of(2024, 7, 1), LocalDate.of(2028, 5, 31), 60);
        batch("IT-A-2024", btechIt.getId(), it.getId(), "2024-25", 5,
              LocalDate.of(2024, 7, 1), LocalDate.of(2028, 5, 31), 60);

        // 2023 intake — Semester 7 (final year)
        batch("CS-A-2023", btechCs.getId(), cs.getId(), "2023-24", 7,
              LocalDate.of(2023, 7, 1), LocalDate.of(2027, 5, 31), 60);

        // M.Tech 2026 batch
        batch("MTECH-CS-2026", mtechCs.getId(), cs.getId(), "2026-27", 1,
              LocalDate.of(2026, 7, 1), LocalDate.of(2028, 5, 31), 30);

        log.info("Seed data complete: " + deptRepo.count() + " depts, " +
                courseRepo.count() + " courses, " + subjectRepo.count() +
                " subjects, " + batchRepo.count() + " batches.");
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
                         int semester, int credits, int lectures, Subject.SubjectType type) {
        Subject s = new Subject();
        s.setName(name); s.setCode(code); s.setCourseId(courseId); s.setDepartmentId(deptId);
        s.setSemester(semester); s.setCredits(credits); s.setTotalLectures(lectures);
        s.setType(type); s.setIsActive(true);
        subjectRepo.save(s);
    }

    private void batch(String name, Long courseId, Long deptId, String academicYear,
                       int currentSemester, LocalDate start, LocalDate end, int maxStrength) {
        Batch b = new Batch();
        b.setName(name); b.setCourseId(courseId); b.setDepartmentId(deptId);
        b.setAcademicYear(academicYear); b.setCurrentSemester(currentSemester);
        b.setStartDate(start); b.setEndDate(end); b.setMaxStrength(maxStrength); b.setIsActive(true);
        batchRepo.save(b);
    }
}
