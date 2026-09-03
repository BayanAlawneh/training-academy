package com.academy.tms.controller;

import com.academy.tms.dto.ApiResponse;
import com.academy.tms.dto.TraineeRequest;
import com.academy.tms.dto.TraineeResponse;
import com.academy.tms.services.TraineeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/trainees")
public class TraineeController {

    private final TraineeService traineeService;

    public TraineeController(TraineeService traineeService) {
        this.traineeService = traineeService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TraineeResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.ok("Trainees loaded", traineeService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TraineeResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Trainee loaded", traineeService.findById(id)));
    }

    /** عدد الكورسات المسجَّل فيها المتدرّب — يُستخدم لرسالة تأكيد الحذف. */
    @GetMapping("/{id}/deletion-check")
    public ResponseEntity<ApiResponse<Long>> deletionCheck(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.ok("Deletion check complete", traineeService.enrollmentCount(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TraineeResponse>> create(@Valid @RequestBody TraineeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Trainee created", traineeService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TraineeResponse>> update(@PathVariable Long id,
                                                               @Valid @RequestBody TraineeRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Trainee updated", traineeService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable Long id) {
        traineeService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Trainee deleted", null));
    }
}