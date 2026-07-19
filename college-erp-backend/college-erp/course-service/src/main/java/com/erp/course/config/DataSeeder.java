package com.erp.course.config;

import com.erp.course.entity.*;
import com.erp.course.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final DepartmentRepository deptRepo;
    private final CourseRepository courseRepo;
    private final SubjectRepository subjectRepo;
    private final BatchRepository batchRepo;

    @Override
    public void run(String... args) {
        if (deptRepo.count() > 0) {
            log.info("Seed data already exists — skipping.");
            return;
        }

        log.info("Seeding initial data...");

        // ── Departments ────────────────────────────────────────────────
        Department cs = deptRepo.save(Department.builder()
                .name("Computer Science").code("CS")
                .description("Department of Computer Science & Engineering").build());
        Department it = deptRepo.save(Department.builder()
                .name("Information Technology").code("IT")
                .description("Department of Information Technology").build());
        Department mech = deptRepo.save(Department.builder()
                .name("Mechanical Engineering").code("MECH")
                .description("Department of Mechanical Engineering").build());
        Department ec = deptRepo.save(Department.builder()
                .name("Electronics & Communication").code("EC")
                .description("Department of Electronics & Communication Engineering").build());

        // ── Courses ────────────────────────────────────────────────────
        Course btech = courseRepo.save(Course.builder()
                .name("Bachelor of Technology").code("BTECH-CS")
                .departmentId(cs.getId()).totalSemesters(8).durationYears(4)
                .type(Course.CourseType.UNDERGRADUATE).build());
        Course mtech = courseRepo.save(Course.builder()
                .name("Master of Technology").code("MTECH-CS")
                .departmentId(cs.getId()).totalSemesters(4).durationYears(2)
                .type(Course.CourseType.POSTGRADUATE).build());

        // ── Subjects — B.Tech Sem 1 ────────────────────────────────────
        subjectRepo.save(Subject.builder().name("Engineering Mathematics I").code("MATH101")
                .courseId(btech.getId()).departmentId(cs.getId())
                .semester(1).credits(4).totalLectures(60).type(Subject.SubjectType.THEORY).build());
        subjectRepo.save(Subject.builder().name("Programming in C").code("CS101")
                .courseId(btech.getId()).departmentId(cs.getId())
                .semester(1).credits(4).totalLectures(60).type(Subject.SubjectType.THEORY).build());
        subjectRepo.save(Subject.builder().name("Programming Lab").code("CS101L")
                .courseId(btech.getId()).departmentId(cs.getId())
                .semester(1).credits(2).totalLectures(30).type(Subject.SubjectType.PRACTICAL).build());
        subjectRepo.save(Subject.builder().name("Engineering Physics").code("PHY101")
                .courseId(btech.getId()).departmentId(cs.getId())
                .semester(1).credits(3).totalLectures(45).type(Subject.SubjectType.THEORY).build());

        // ── Subjects — B.Tech Sem 2 ────────────────────────────────────
        subjectRepo.save(Subject.builder().name("Data Structures").code("CS201")
                .courseId(btech.getId()).departmentId(cs.getId())
                .semester(2).credits(4).totalLectures(60).type(Subject.SubjectType.THEORY).build());
        subjectRepo.save(Subject.builder().name("Data Structures Lab").code("CS201L")
                .courseId(btech.getId()).departmentId(cs.getId())
                .semester(2).credits(2).totalLectures(30).type(Subject.SubjectType.PRACTICAL).build());
        subjectRepo.save(Subject.builder().name("Engineering Mathematics II").code("MATH201")
                .courseId(btech.getId()).departmentId(cs.getId())
                .semester(2).credits(4).totalLectures(60).type(Subject.SubjectType.THEORY).build());
        subjectRepo.save(Subject.builder().name("Digital Electronics").code("EC201")
                .courseId(btech.getId()).departmentId(cs.getId())
                .semester(2).credits(3).totalLectures(45).type(Subject.SubjectType.THEORY).build());

        // ── Subjects — B.Tech Sem 3 ────────────────────────────────────
        subjectRepo.save(Subject.builder().name("Object Oriented Programming").code("CS301")
                .courseId(btech.getId()).departmentId(cs.getId())
                .semester(3).credits(4).totalLectures(60).type(Subject.SubjectType.THEORY).build());
        subjectRepo.save(Subject.builder().name("Database Management Systems").code("CS302")
                .courseId(btech.getId()).departmentId(cs.getId())
                .semester(3).credits(4).totalLectures(60).type(Subject.SubjectType.THEORY).build());
        subjectRepo.save(Subject.builder().name("Operating Systems").code("CS303")
                .courseId(btech.getId()).departmentId(cs.getId())
                .semester(3).credits(4).totalLectures(60).type(Subject.SubjectType.THEORY).build());
        subjectRepo.save(Subject.builder().name("DBMS Lab").code("CS302L")
                .courseId(btech.getId()).departmentId(cs.getId())
                .semester(3).credits(2).totalLectures(30).type(Subject.SubjectType.PRACTICAL).build());

        // ── Batches ────────────────────────────────────────────────────
        batchRepo.save(Batch.builder()
                .name("CS-A-2024").courseId(btech.getId()).departmentId(cs.getId())
                .academicYear("2024-25").currentSemester(1)
                .startDate(LocalDate.of(2024, 8, 1)).endDate(LocalDate.of(2028, 5, 31))
                .maxStrength(60).build());
        batchRepo.save(Batch.builder()
                .name("CS-B-2024").courseId(btech.getId()).departmentId(cs.getId())
                .academicYear("2024-25").currentSemester(1)
                .startDate(LocalDate.of(2024, 8, 1)).endDate(LocalDate.of(2028, 5, 31))
                .maxStrength(60).build());
        batchRepo.save(Batch.builder()
                .name("CS-A-2023").courseId(btech.getId()).departmentId(cs.getId())
                .academicYear("2023-24").currentSemester(3)
                .startDate(LocalDate.of(2023, 8, 1)).endDate(LocalDate.of(2027, 5, 31))
                .maxStrength(60).build());

        log.info("Seed data inserted: {} departments, {} courses, {} subjects, {} batches",
                deptRepo.count(), courseRepo.count(), subjectRepo.count(), batchRepo.count());
    }
}
