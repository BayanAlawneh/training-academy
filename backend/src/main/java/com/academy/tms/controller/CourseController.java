package com.academy.tms.controller;

import com.academy.tms.dto.ApiResponse;
import com.academy.tms.dto.CourseRequest;
import com.academy.tms.dto.CourseResponse;
import com.academy.tms.services.CourseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.ok("Courses loaded", courseService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Course loaded", courseService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CourseResponse>> create(@Valid @RequestBody CourseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Course created", courseService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> update(@PathVariable Long id,
                                                              @Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Course updated", courseService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable Long id) {
        courseService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Course deleted", null));
    }
}
