package com.erp.admin.config;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "student-service", path = "/api/students")
interface StudentClient {
    @GetMapping("/count/active") Long countActiveStudents();
    @GetMapping Map<String, Object> getAllStudents(@RequestParam int page, @RequestParam int size, @RequestParam String sort);
}

@FeignClient(name = "faculty-service", path = "/api/faculty")
interface FacultyClient {
    @GetMapping("/count/active") Long countActiveFaculty();
}

@FeignClient(name = "attendance-service", path = "/api/attendance")
interface AttendanceClient {
    @GetMapping("/stats/today") Map<String, Object> getTodayStats();
    @GetMapping("/stats/trend") List<Map<String, Object>> getAttendanceTrend(@RequestParam int days);
}

@FeignClient(name = "course-service", path = "/api/departments")
interface DepartmentClient {
    @GetMapping List<Map<String, Object>> getAllDepartments();
}

@FeignClient(name = "notification-service", path = "/api/notifications")
interface NotificationClient {
    @GetMapping("/send") void send(@org.springframework.web.bind.annotation.RequestBody Map<String, Object> payload);
}
