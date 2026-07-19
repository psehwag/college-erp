package com.erp.admin.config;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "course-service")
public interface CourseFeignClient {

    @GetMapping("/api/departments")
    List<Map<String, Object>> getAllDepartments();

    @GetMapping("/api/departments/{deptId}/courses")
    List<Map<String, Object>> getCoursesByDept(@PathVariable Long deptId);

    @GetMapping("/api/departments/{deptId}/batches")
    List<Map<String, Object>> getBatchesByDept(@PathVariable Long deptId);
}
