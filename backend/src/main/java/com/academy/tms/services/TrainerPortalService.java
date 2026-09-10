package com.academy.tms.services;

import com.academy.tms.dto.*;
import com.academy.tms.entities.*;
import com.academy.tms.exception.ResourceNotFoundException;
import com.academy.tms.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * بوابة المدرّب: كورسه، جلساته، وتسجيل الحضور.
 *
 * كل عملية تتحقّق من أن الجلسة تخصّ كورس هذا المدرّب. الاعتماد على دور
 * TRAINER وحده لا يكفي — فهو يمنع المتدربين لكنه لا يمنع مدرّباً من
 * تعديل جلسات مدرّب آخر لو مرّر معرّفاً ليس له.
 */
@Service
public class TrainerPortalService {

    private final TrainerRepository trainerRepository;
    private final CourseRepository courseRepository;
    private final SessionRepository sessionRepository;
    private final AttendanceRepository attendanceRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final TraineeRepository traineeRepository;
    private final NotificationService notificationService;

    public TrainerPortalService(TrainerRepository trainerRepository,
                                CourseRepository courseRepository,
                                SessionRepository sessionRepository,
                                AttendanceRepository attendanceRepository,
                                EnrollmentRepository enrollmentRepository,
                                TraineeRepository traineeRepository,
                                NotificationService notificationService) {
        this.trainerRepository = trainerRepository;
        this.courseRepository = courseRepository;
        this.sessionRepository = sessionRepository;
        this.attendanceRepository = attendanceRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.traineeRepository = traineeRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public TrainerSummaryResponse summary(String email) {
        Trainer trainer = loadTrainer(email);
        List<Course> courses = courseRepository.findAllByTrainerId(trainer.getId());

        if (courses.isEmpty()) {
            return new TrainerSummaryResponse(
                    trainer.getUser().getName(), trainer.getUser().getEmail(),
                    trainer.getSpecialization(), null, null, 0, 0);
        }

        Course course = courses.get(0);
        return new TrainerSummaryResponse(
                trainer.getUser().getName(),
                trainer.getUser().getEmail(),
                trainer.getSpecialization(),
                course.getId(),
                course.getTitle(),
                enrollmentRepository.countByCourseId(course.getId()),
                sessionRepository.countByCourseId(course.getId())
        );
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> mySessions(String email) {
        Course course = loadMyCourse(email);
        return sessionRepository.findAllByCourseId(course.getId()).stream()
                .map(SessionResponse::from)
                .toList();
    }

    @Transactional
    public SessionResponse createSession(String email, SessionRequest request) {
        Course course = loadMyCourse(email);
        validateTimes(request.getStartTime(), request.getEndTime());

        TrainingSession session = new TrainingSession(
                course, request.getTitle(), request.getSessionDate(),
                request.getStartTime(), request.getEndTime(), request.getMeetingLink());

        return SessionResponse.from(sessionRepository.save(session));
    }

    /**
     * توليد جدول لقاءات دفعة واحدة: حلقة على الأيام بين تاريخ البداية والنهاية،
     * وإنشاء جلسة عند كل يوم مطابق للأيام المختارة. لا cron ولا مكتبات.
     */
    @Transactional
    public List<SessionResponse> generateSchedule(String email, ScheduleRequest request) {
        Course course = loadMyCourse(email);
        validateTimes(request.getStartTime(), request.getEndTime());

        Set<DayOfWeek> days = new HashSet<>();
        for (Integer value : request.getWeekdays()) {
            if (value == null || value < 1 || value > 7) {
                throw new IllegalArgumentException("Weekday must be between 1 (Mon) and 7 (Sun)");
            }
            days.add(DayOfWeek.of(value));
        }

        String prefix = (request.getTitlePrefix() == null || request.getTitlePrefix().isBlank())
                ? "لقاء" : request.getTitlePrefix().trim();

        LocalDate cursor = request.getStartDate();
        LocalDate end = request.getStartDate().plusWeeks(request.getWeeks());

        List<TrainingSession> created = new ArrayList<>();
        int number = 1;

        while (cursor.isBefore(end)) {
            if (days.contains(cursor.getDayOfWeek())) {
                created.add(new TrainingSession(
                        course, prefix + " " + number, cursor,
                        request.getStartTime(), request.getEndTime(), null));
                number++;
            }
            cursor = cursor.plusDays(1);
        }

        if (created.isEmpty()) {
            throw new IllegalArgumentException(
                    "No sessions fall in this range. Check the weekdays and duration.");
        }

        List<TrainingSession> saved = sessionRepository.saveAll(created);

        notificationService.notifyCourseTrainees(
                course.getId(),
                "جدول لقاءات جديد",
                "تمّت جدولة " + saved.size() + " لقاء في كورس " + course.getTitle()
                        + ". راجع صفحة حضوري للمواعيد.",
                "/trainee/attendance",
                NotificationType.SESSION_SCHEDULED);

        return saved.stream()
                .map(SessionResponse::from)
                .toList();
    }

    @Transactional
    public SessionResponse updateSession(String email, Long sessionId, SessionRequest request) {
        TrainingSession session = loadOwnedSession(email, sessionId);
        validateTimes(request.getStartTime(), request.getEndTime());

        session.setTitle(request.getTitle());
        session.setSessionDate(request.getSessionDate());
        session.setStartTime(request.getStartTime());
        session.setEndTime(request.getEndTime());
        session.setMeetingLink(request.getMeetingLink());

        return SessionResponse.from(session);
    }

    @Transactional
    public void deleteSession(String email, Long sessionId) {
        TrainingSession session = loadOwnedSession(email, sessionId);

        attendanceRepository.deleteAllBySessionId(sessionId);
        attendanceRepository.flush();

        sessionRepository.delete(session);
    }

    /** قائمة متدربي الكورس مع حالتهم المسجَّلة في هذه الجلسة. */
    @Transactional(readOnly = true)
    public List<AttendanceRowResponse> sessionRoster(String email, Long sessionId) {
        TrainingSession session = loadOwnedSession(email, sessionId);

        List<Attendance> recorded = attendanceRepository.findAllBySessionId(sessionId);

        return enrollmentRepository.findAllByCourseIdWithDetails(session.getCourse().getId()).stream()
                .map(enrollment -> {
                    Trainee trainee = enrollment.getTrainee();
                    Attendance match = recorded.stream()
                            .filter(a -> a.getTrainee().getId().equals(trainee.getId()))
                            .findFirst()
                            .orElse(null);

                    return new AttendanceRowResponse(
                            trainee.getId(),
                            trainee.getUser().getName(),
                            trainee.getUser().getEmail(),
                            match != null ? match.getStatus().name() : "NOT_RECORDED",
                            match != null ? match.getNote() : null);
                })
                .toList();
    }

    @Transactional
    public List<AttendanceRowResponse> markAttendance(String email, Long sessionId,
                                                      AttendanceMarkRequest request) {
        TrainingSession session = loadOwnedSession(email, sessionId);

        for (AttendanceMarkRequest.Entry entry : request.getEntries()) {
            if (entry.getTraineeId() == null || entry.getStatus() == null) continue;
            if ("NOT_RECORDED".equals(entry.getStatus())) continue;

            AttendanceStatus status;
            try {
                status = AttendanceStatus.valueOf(entry.getStatus());
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Unknown attendance status: " + entry.getStatus());
            }

            if (!enrollmentRepository.existsByTraineeIdAndCourseId(
                    entry.getTraineeId(), session.getCourse().getId())) {
                throw new IllegalArgumentException(
                        "Trainee " + entry.getTraineeId() + " is not enrolled in this course");
            }

            Attendance existing = attendanceRepository
                    .findBySessionIdAndTraineeId(sessionId, entry.getTraineeId())
                    .orElse(null);

            if (existing != null) {
                existing.setStatus(status);
                existing.setNote(entry.getNote());
            } else {
                Trainee trainee = traineeRepository.findByIdWithUser(entry.getTraineeId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Trainee not found: " + entry.getTraineeId()));
                attendanceRepository.save(
                        new Attendance(session, trainee, status, entry.getNote()));
            }
        }

        session.setStatus(SessionStatus.DONE);
        attendanceRepository.flush();

        notificationService.notifyCourseTrainees(
                session.getCourse().getId(),
                "تمّ تسجيل الحضور",
                "سُجّل حضور جلسة \"" + session.getTitle() + "\". راجع صفحة حضوري.",
                "/trainee/attendance",
                NotificationType.ATTENDANCE_RECORDED);

        return sessionRoster(email, sessionId);
    }

    // ---------- مساعدات ----------

    private void validateTimes(java.time.LocalTime start, java.time.LocalTime end) {
        if (start != null && end != null && !end.isAfter(start)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
    }

    private Trainer loadTrainer(String email) {
        return trainerRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No trainer profile is linked to this account"));
    }

    private Course loadMyCourse(String email) {
        Trainer trainer = loadTrainer(email);
        List<Course> courses = courseRepository.findAllByTrainerId(trainer.getId());

        if (courses.isEmpty()) {
            throw new ResourceNotFoundException("No course is assigned to you yet");
        }
        return courses.get(0);
    }

    /** يمنع مدرّباً من لمس جلسة لا تخصّ كورسه. */
    private TrainingSession loadOwnedSession(String email, Long sessionId) {
        TrainingSession session = sessionRepository.findByIdWithCourse(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        Trainer trainer = loadTrainer(email);
        if (!session.getCourse().getTrainer().getId().equals(trainer.getId())) {
            throw new AccessDeniedException("This session does not belong to your course");
        }
        return session;
    }
}
