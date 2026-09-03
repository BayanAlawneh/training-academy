package com.academy.tms.controller;

import com.academy.tms.dto.*;
import com.academy.tms.services.ExamService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** اختبارات المدرّب — تحت /api/trainer المحمي بدور TRAINER. */
@RestController
@RequestMapping("/api/trainer/me/exams")
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ExamResponse>>> list(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok("Exams loaded", examService.myExams(auth.getName())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExamResponse>> detail(Authentication auth, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Exam loaded", examService.examDetail(auth.getName(), id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ExamResponse>> create(
            Authentication auth, @Valid @RequestBody ExamRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Exam created", examService.create(auth.getName(), request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExamResponse>> update(
            Authentication auth, @PathVariable Long id, @Valid @RequestBody ExamRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Exam updated", examService.update(auth.getName(), id, request)));
    }

    @PutMapping("/{id}/published")
    public ResponseEntity<ApiResponse<ExamResponse>> publish(
            Authentication auth, @PathVariable Long id, @RequestParam boolean value) {
        return ResponseEntity.ok(ApiResponse.ok(
                value ? "Exam published" : "Exam unpublished",
                examService.setPublished(auth.getName(), id, value)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> delete(Authentication auth, @PathVariable Long id) {
        examService.delete(auth.getName(), id);
        return ResponseEntity.ok(ApiResponse.ok("Exam deleted", null));
    }

    @GetMapping("/{id}/grades")
    public ResponseEntity<ApiResponse<List<GradeRowResponse>>> grades(
            Authentication auth, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Grades loaded", examService.grades(auth.getName(), id)));
    }
}
