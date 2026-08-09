package com.academy.tms.controller;

import com.academy.tms.dto.ApiResponse;
import com.academy.tms.dto.EnrollmentRequest;
import com.academy.tms.dto.EnrollmentResponse;
import com.academy.tms.services.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> findByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(
                ApiResponse.ok("Enrollments loaded", enrollmentService.findByCourse(courseId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enrol(@Valid @RequestBody EnrollmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Trainee enrolled", enrollmentService.enrol(request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> remove(@PathVariable Long id) {
        enrollmentService.remove(id);
        return ResponseEntity.ok(ApiResponse.ok("Enrollment removed", null));
    }
}
