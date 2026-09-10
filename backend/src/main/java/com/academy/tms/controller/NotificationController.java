package com.academy.tms.controller;

import com.academy.tms.dto.ApiResponse;
import com.academy.tms.dto.NotificationResponse;
import com.academy.tms.services.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * متاح لكل مستخدم مسجَّل الدخول بأي دور — يقع تحت anyRequest().authenticated()
 * في SecurityConfig فلا يحتاج قاعدة جديدة. والهوية من التوكن لا من الرابط.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> list(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Notifications loaded", notificationService.myNotifications(auth.getName())));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> unread(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok("Unread count",
                Map.of("count", notificationService.unreadCount(auth.getName()))));
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Object>> markAllRead(Authentication auth) {
        notificationService.markAllRead(auth.getName());
        return ResponseEntity.ok(ApiResponse.ok("All marked as read", null));
    }
}
