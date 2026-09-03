package com.academy.tms.controller;

import com.academy.tms.dto.ApiResponse;
import com.academy.tms.dto.MyCourseResponse;
import com.academy.tms.dto.MySummaryResponse;
import com.academy.tms.services.TraineePortalService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * مسارات المتدرّب عن نفسه. SecurityConfig يحمي /api/trainee/** بدور TRAINEE،
 * والهوية تُقرأ من التوكن عبر Authentication — لا يوجد أي معرّف في الرابط،
 * فلا يمكن لمتدرّب أن يطلب بيانات متدرّب آخر.
 */
@RestController
@RequestMapping("/api/trainee")
public class TraineePortalController {

    private final TraineePortalService portalService;

    public TraineePortalController(TraineePortalService portalService) {
        this.portalService = portalService;
    }

    @GetMapping("/me/courses")
    public ResponseEntity<ApiResponse<List<MyCourseResponse>>> myCourses(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(
                "My courses loaded", portalService.myCourses(authentication.getName())));
    }

    @GetMapping("/me/summary")
    public ResponseEntity<ApiResponse<MySummaryResponse>> mySummary(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Summary loaded", portalService.mySummary(authentication.getName())));
    }
}
