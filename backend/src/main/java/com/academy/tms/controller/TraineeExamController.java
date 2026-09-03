package com.academy.tms.controller;

import com.academy.tms.dto.*;
import com.academy.tms.services.TraineeExamService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** اختبارات المتدرّب — تحت /api/trainee المحمي بدور TRAINEE. */
@RestController
@RequestMapping("/api/trainee/me/exams")
public class TraineeExamController {

    private final TraineeExamService examService;

    public TraineeExamController(TraineeExamService examService) {
        this.examService = examService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MyExamResponse>>> list(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok("Exams loaded", examService.myExams(auth.getName())));
    }

    @GetMapping("/{id}/paper")
    public ResponseEntity<ApiResponse<ExamPaperResponse>> paper(
            Authentication auth, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Paper loaded", examService.openPaper(auth.getName(), id)));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<SubmissionResultResponse>> submit(
            Authentication auth, @PathVariable Long id, @Valid @RequestBody SubmitExamRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Exam submitted", examService.submit(auth.getName(), id, request)));
    }
}
