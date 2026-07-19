package com.erp.admin.config;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "attendance-service")
public interface AttendanceFeignClient {

    @GetMapping("/api/attendance/student/{studentId}/subject/{subjectId}/percentage")
    Map<String, Object> getAttendancePercentage(
            @PathVariable Long studentId,
            @PathVariable Long subjectId);

    @GetMapping("/api/attendance/subject/{subjectId}/defaulters")
    List<Long> getDefaulters(
            @PathVariable Long subjectId,
            @RequestParam(required = false) Double threshold);
}
