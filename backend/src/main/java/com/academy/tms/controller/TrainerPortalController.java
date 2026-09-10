package com.academy.tms.controller;

import com.academy.tms.dto.*;
import com.academy.tms.services.CourseService;
import com.academy.tms.services.TrainerPortalService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * مسارات المدرّب عن نفسه. محمي بدور TRAINER في SecurityConfig،
 * والهوية تُقرأ من التوكن لا من الرابط.
 */
@RestController
@RequestMapping("/api/trainer")
public class TrainerPortalController {

    private final TrainerPortalService portalService;
    private final CourseService courseService;

    public TrainerPortalController(TrainerPortalService portalService,
                                   CourseService courseService) {
        this.portalService = portalService;
        this.courseService = courseService;
    }

    /** المدرّب يكتب وصف كورسه ومدّته — لا يملك تعديل السعة ولا المدرّب. */
    @PutMapping("/me/course/details")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourseDetails(
            Authentication auth, @RequestParam Long courseId,
            @Valid @RequestBody CourseDetailsRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Course details updated",
                courseService.updateDetails(auth.getName(), courseId, request)));
    }

    @GetMapping("/me/summary")
    public ResponseEntity<ApiResponse<TrainerSummaryResponse>> summary(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Summary loaded", portalService.summary(auth.getName())));
    }

    @GetMapping("/me/sessions")
    public ResponseEntity<ApiResponse<List<SessionResponse>>> sessions(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Sessions loaded", portalService.mySessions(auth.getName())));
    }

    @PostMapping("/me/sessions")
    public ResponseEntity<ApiResponse<SessionResponse>> create(
            Authentication auth, @Valid @RequestBody SessionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Session created", portalService.createSession(auth.getName(), request)));
    }

    @PostMapping("/me/sessions/generate")
    public ResponseEntity<ApiResponse<List<SessionResponse>>> generate(
            Authentication auth, @Valid @RequestBody ScheduleRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Schedule generated", portalService.generateSchedule(auth.getName(), request)));
    }

    @PutMapping("/me/sessions/{id}")
    public ResponseEntity<ApiResponse<SessionResponse>> update(
            Authentication auth, @PathVariable Long id, @Valid @RequestBody SessionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Session updated", portalService.updateSession(auth.getName(), id, request)));
    }

    @DeleteMapping("/me/sessions/{id}")
    public ResponseEntity<ApiResponse<Object>> delete(Authentication auth, @PathVariable Long id) {
        portalService.deleteSession(auth.getName(), id);
        return ResponseEntity.ok(ApiResponse.ok("Session deleted", null));
    }

    @GetMapping("/me/sessions/{id}/attendance")
    public ResponseEntity<ApiResponse<List<AttendanceRowResponse>>> roster(
            Authentication auth, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Roster loaded", portalService.sessionRoster(auth.getName(), id)));
    }

    @PutMapping("/me/sessions/{id}/attendance")
    public ResponseEntity<ApiResponse<List<AttendanceRowResponse>>> mark(
            Authentication auth, @PathVariable Long id,
            @Valid @RequestBody AttendanceMarkRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Attendance saved", portalService.markAttendance(auth.getName(), id, request)));
    }
}
