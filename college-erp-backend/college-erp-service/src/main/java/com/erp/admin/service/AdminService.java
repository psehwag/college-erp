package com.erp.admin.service;

import com.erp.attendance.entity.Attendance;
import com.erp.attendance.repository.AttendanceRepository;
import com.erp.auth.entity.User;
import com.erp.auth.repository.UserRepository;
import com.erp.course.repository.BatchRepository;
import com.erp.course.repository.DepartmentRepository;
import com.erp.faculty.repository.FacultyRepository;
import com.erp.student.entity.Student;
import com.erp.student.repository.StudentRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AdminService {

    private static final Logger log = Logger.getLogger(AdminService.class.getName());

    private final StudentRepository    studentRepo;
    private final FacultyRepository    facultyRepo;
    private final DepartmentRepository deptRepo;
    private final BatchRepository      batchRepo;
    private final AttendanceRepository attendanceRepo;
    private final UserRepository       userRepo;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public AdminService(StudentRepository studentRepo,
                        FacultyRepository facultyRepo,
                        DepartmentRepository deptRepo,
                        BatchRepository batchRepo,
                        AttendanceRepository attendanceRepo,
                        UserRepository userRepo,
                        KafkaTemplate<String, Object> kafkaTemplate) {
        this.studentRepo    = studentRepo;
        this.facultyRepo    = facultyRepo;
        this.deptRepo       = deptRepo;
        this.batchRepo      = batchRepo;
        this.attendanceRepo = attendanceRepo;
        this.userRepo       = userRepo;
        this.kafkaTemplate  = kafkaTemplate;
    }

    public Map<String, Object> getDashboard() {
        long totalStudents   = studentRepo.countActive();
        long totalFaculty    = facultyRepo.countActive();
        long totalDepts      = deptRepo.count();
        long totalBatches    = batchRepo.count();
        long totalUsers      = userRepo.count();

        // ── Today's attendance (real, not dummy) ────────────────────────
        LocalDate today = LocalDate.now();
        long totalMarkedToday   = attendanceRepo.countByAttendanceDate(today);
        long presentToday       = attendanceRepo.countByAttendanceDateAndStatus(today, Attendance.AttendanceStatus.PRESENT);
        double todayPercentage  = totalMarkedToday > 0 ? Math.round((presentToday * 100.0 / totalMarkedToday) * 10.0) / 10.0 : 0.0;

        // ── Last 7 days attendance trend (real) ─────────────────────────
        List<Map<String, Object>> weeklyTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            long total   = attendanceRepo.countByAttendanceDate(day);
            long present = attendanceRepo.countByAttendanceDateAndStatus(day, Attendance.AttendanceStatus.PRESENT);
            long absent  = total - present;
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", day.toString());
            point.put("day", day.getDayOfWeek().toString().substring(0, 3));
            point.put("present", present);
            point.put("absent", Math.max(absent, 0));
            weeklyTrend.add(point);
        }

        // ── Department breakdown (real student/faculty counts, no random) ─
        List<Map<String, Object>> deptStats = new ArrayList<>();
        deptRepo.findByIsActiveTrue().forEach(dept -> {
            Map<String, Object> ds = new LinkedHashMap<>();
            ds.put("id",   dept.getId());
            ds.put("name", dept.getName());
            ds.put("code", dept.getCode());
            ds.put("studentCount", studentRepo.countActiveByDept(dept.getId()));
            ds.put("facultyCount",
                    facultyRepo.findByDepartmentIdAndStatus(
                            dept.getId(),
                            com.erp.faculty.entity.Faculty.FacultyStatus.ACTIVE).size());
            deptStats.add(ds);
        });

        // ── Recently registered students (real, last 5) ────────────────
        List<Map<String, Object>> recentStudents = studentRepo
                .findAll(org.springframework.data.domain.PageRequest.of(0, 5,
                        org.springframework.data.domain.Sort.by("createdAt").descending()))
                .stream()
                .map(s -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", s.getId());
                    m.put("fullName", s.getFullName());
                    m.put("enrollmentNumber", s.getEnrollmentNumber());
                    m.put("departmentId", s.getDepartmentId());
                    m.put("createdAt", s.getCreatedAt());
                    return m;
                }).collect(Collectors.toList());

        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("totalStudents",  totalStudents);
        dashboard.put("totalFaculty",   totalFaculty);
        dashboard.put("totalDepartments", totalDepts);
        dashboard.put("totalBatches",   totalBatches);
        dashboard.put("totalUsers",     totalUsers);
        dashboard.put("todayPresent",   presentToday);
        dashboard.put("todayTotalMarked", totalMarkedToday);
        dashboard.put("todayPercentage", todayPercentage);
        dashboard.put("weeklyTrend",    weeklyTrend);
        dashboard.put("departmentStats", deptStats);
        dashboard.put("recentStudents", recentStudents);
        return dashboard;
    }

    @Transactional
    public void broadcastAnnouncement(String title, String message, List<String> targetRoles, boolean sendEmail) {
        if (targetRoles == null || targetRoles.isEmpty()) {
            throw new com.erp.common.exception.AppException(
                    "Select at least one target role", org.springframework.http.HttpStatus.BAD_REQUEST);
        }

        List<User.Role> roles = targetRoles.stream()
                .map(r -> {
                    try { return User.Role.valueOf(r); }
                    catch (Exception e) { return null; }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (roles.isEmpty()) {
            throw new com.erp.common.exception.AppException(
                    "No valid target roles provided", org.springframework.http.HttpStatus.BAD_REQUEST);
        }

        // Fan out: one notification per active user with a matching role.
        // Inactive users are explicitly excluded by the repository query.
        List<User> recipients = userRepo.findByRoleInAndIsActiveTrue(roles);

        for (User u : recipients) {
            Map<String, Object> event = new LinkedHashMap<>();
            event.put("type",           "GENERAL_ANNOUNCEMENT");
            event.put("title",          title);
            event.put("message",        message);
            event.put("recipientId",    u.getId());
            event.put("recipientEmail", u.getEmail());
            event.put("recipientRole",  u.getRole().name());
            event.put("sendEmail",      sendEmail);
            kafkaTemplate.send("general-events", event);
        }

        log.info("Broadcast sent: \"" + title + "\" to " + recipients.size() +
                " active user(s) across roles " + roles);
    }

    public List<Long> getDefaulters(Long subjectId, Double threshold) {
        return attendanceRepo.findDefaulters(subjectId, threshold != null ? threshold : 75.0);
    }

    public List<Long> getDefaultersByBatch(Long subjectId, Long batchId, Double threshold) {
        return attendanceRepo.findDefaultersByBatch(subjectId, batchId, threshold != null ? threshold : 75.0);
    }
}
