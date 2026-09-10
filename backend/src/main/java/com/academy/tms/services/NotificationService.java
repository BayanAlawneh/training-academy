package com.academy.tms.services;

import com.academy.tms.dto.NotificationResponse;
import com.academy.tms.entities.Notification;
import com.academy.tms.entities.NotificationType;
import com.academy.tms.entities.User;
import com.academy.tms.repository.EnrollmentRepository;
import com.academy.tms.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * طبقة إشعارات واحدة يستدعيها كل من ينشر اختباراً أو يجدول جلسة.
 *
 * إن أُضيف البريد لاحقاً، يُضاف هنا خلف نفس الدوال — فلا يتغيّر أي مستدعٍ.
 * وبدأنا بالإشعار داخل النظام لا بالبريد لأنه لا يحتاج SMTP ولا اتصالاً،
 * فلا يتعطّل أثناء العرض.
 */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EnrollmentRepository enrollmentRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               EnrollmentRepository enrollmentRepository) {
        this.notificationRepository = notificationRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> myNotifications(String email) {
        return notificationRepository.findRecentByEmail(email).stream()
                .limit(40)
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public long unreadCount(String email) {
        return notificationRepository.countUnreadByEmail(email);
    }

    @Transactional
    public void markAllRead(String email) {
        notificationRepository.markAllReadByEmail(email);
    }

    @Transactional
    public void notifyUser(User user, String title, String body, String link, NotificationType type) {
        notificationRepository.save(new Notification(user, title, body, link, type));
    }

    /**
     * إشعار كل متدربي كورس. يُستدعى عند نشر اختبار أو توليد جدول جلسات.
     * لا يرمي استثناءً أبداً: فشل الإشعار يجب ألا يُلغي العملية الأصلية.
     */
    @Transactional
    public void notifyCourseTrainees(Long courseId, String title, String body,
                                     String link, NotificationType type) {
        try {
            enrollmentRepository.findAllByCourseIdWithDetails(courseId).forEach(enrollment ->
                    notificationRepository.save(new Notification(
                            enrollment.getTrainee().getUser(), title, body, link, type)));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
