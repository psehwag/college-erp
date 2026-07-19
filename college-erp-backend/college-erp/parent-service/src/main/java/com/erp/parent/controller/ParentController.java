package com.erp.parent.controller;

import com.erp.parent.dto.ParentDto;
import com.erp.parent.service.ParentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parents")
@RequiredArgsConstructor
@Tag(name = "Parents", description = "Parent portal endpoints")
public class ParentController {

    private final ParentService parentService;

    @PostMapping
    @Operation(summary = "Register a parent")
    public ResponseEntity<ParentDto.ApiResponse<ParentDto.Response>> create(
            @Valid @RequestBody ParentDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ParentDto.ApiResponse.success("Parent registered", parentService.createParent(req)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get parent profile")
    public ResponseEntity<ParentDto.ApiResponse<ParentDto.Response>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ParentDto.ApiResponse.success("Parent fetched", parentService.getById(id)));
    }

    @GetMapping("/me")
    @Operation(summary = "Get own parent profile (from JWT header)")
    public ResponseEntity<ParentDto.ApiResponse<ParentDto.Response>> getMe(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ParentDto.ApiResponse.success("Profile fetched", parentService.getById(userId)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update parent profile")
    public ResponseEntity<ParentDto.ApiResponse<ParentDto.Response>> update(
            @PathVariable Long id, @Valid @RequestBody ParentDto.CreateRequest req) {
        return ResponseEntity.ok(ParentDto.ApiResponse.success("Profile updated", parentService.update(id, req)));
    }
}
