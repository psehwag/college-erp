package com.erp.admin.config;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@FeignClient(name = "faculty-service")
public interface FacultyFeignClient {
    @GetMapping("/api/faculty")
    Map<String, Object> getAllFaculty(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam String sort);

    @GetMapping("/api/faculty/department/{deptId}")
    List<Map<String, Object>> getFacultyByDepartment(@PathVariable Long deptId);
}
