package com.academy.tms.controller;

import com.academy.tms.dto.ApiResponse;
import com.academy.tms.dto.MyAttendanceResponse;
import com.academy.tms.dto.MyCourseResponse;
import com.academy.tms.dto.MySummaryResponse;
import com.academy.tms.services.TraineePortalService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/trainee")
public class TraineePortalController {

    private final TraineePortalService portalService;

    public TraineePortalController(TraineePortalService portalService) {
        this.portalService = portalService;
    }

    @GetMapping("/me/courses")
    public ResponseEntity<ApiResponse<List<MyCourseResponse>>> myCourses(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(
                "My courses loaded", portalService.myCourses(auth.getName())));
    }

    @GetMapping("/me/summary")
    public ResponseEntity<ApiResponse<MySummaryResponse>> mySummary(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Summary loaded", portalService.mySummary(auth.getName())));
    }

    @GetMapping("/me/attendance")
    public ResponseEntity<ApiResponse<List<MyAttendanceResponse>>> myAttendance(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Attendance loaded", portalService.myAttendance(auth.getName())));
    }
}
