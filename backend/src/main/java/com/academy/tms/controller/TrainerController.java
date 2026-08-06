package com.academy.tms.controller;

import com.academy.tms.dto.ApiResponse;
import com.academy.tms.dto.TrainerRequest;
import com.academy.tms.dto.TrainerResponse;
import com.academy.tms.services.TrainerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/trainers")
public class TrainerController {

    private final TrainerService trainerService;

    public TrainerController(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TrainerResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.ok("Trainers loaded", trainerService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TrainerResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Trainer loaded", trainerService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TrainerResponse>> create(@Valid @RequestBody TrainerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Trainer created", trainerService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TrainerResponse>> update(@PathVariable Long id,
                                                               @Valid @RequestBody TrainerRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Trainer updated", trainerService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable Long id) {
        trainerService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Trainer deleted", null));
    }
}