package com.erp.admin.config;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@FeignClient(name = "student-service")
public interface StudentFeignClient {

    @GetMapping("/api/students")
    Map<String, Object> getAllStudents(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam String sort);

    @GetMapping("/api/students/department/{deptId}")
    List<Map<String, Object>> getStudentsByDepartment(@PathVariable Long deptId);

    @GetMapping("/api/students/batch/{batchId}")
    List<Map<String, Object>> getStudentsByBatch(@PathVariable Long batchId);
}
