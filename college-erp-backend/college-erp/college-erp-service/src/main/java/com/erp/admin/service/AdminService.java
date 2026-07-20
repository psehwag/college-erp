package com.erp.admin.service;

import com.erp.attendance.repository.AttendanceRepository;
import com.erp.auth.repository.UserRepository;
import com.erp.course.repository.BatchRepository;
import com.erp.course.repository.DepartmentRepository;
import com.erp.faculty.repository.FacultyRepository;
import com.erp.student.repository.StudentRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;

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

        // Department breakdown
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

        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("totalStudents",  totalStudents);
        dashboard.put("totalFaculty",   totalFaculty);
        dashboard.put("totalDepartments", totalDepts);
        dashboard.put("totalBatches",   totalBatches);
        dashboard.put("totalUsers",     totalUsers);
        dashboard.put("departmentStats", deptStats);
        return dashboard;
    }

    @Transactional
    public void broadcastAnnouncement(String title, String message, String targetRole) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("type",          "GENERAL_ANNOUNCEMENT");
        event.put("title",         title);
        event.put("message",       message);
        event.put("recipientRole", targetRole);
        event.put("sendEmail",     false);
        kafkaTemplate.send("general-events", event);
        log.info("Broadcast sent: " + title + " -> " + targetRole);
    }

    public List<Long> getDefaulters(Long subjectId, Double threshold) {
        return attendanceRepo.findDefaulters(subjectId, threshold != null ? threshold : 75.0);
    }
}
