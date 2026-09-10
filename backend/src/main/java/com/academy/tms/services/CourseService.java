package com.academy.tms.services;

import com.academy.tms.dto.CourseDetailsRequest;
import com.academy.tms.dto.CourseRequest;
import com.academy.tms.dto.CourseResponse;
import com.academy.tms.entities.Course;
import com.academy.tms.entities.NotificationType;
import com.academy.tms.entities.Trainer;
import com.academy.tms.repository.TrainerRepository;
import com.academy.tms.exception.DuplicateResourceException;
import com.academy.tms.exception.ResourceNotFoundException;
import com.academy.tms.repository.CourseRepository;
import com.academy.tms.repository.EnrollmentRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final TrainerRepository trainerRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final NotificationService notificationService;

    public CourseService(CourseRepository courseRepository,
                         TrainerRepository trainerRepository,
                         EnrollmentRepository enrollmentRepository,
                         NotificationService notificationService) {
        this.courseRepository = courseRepository;
        this.trainerRepository = trainerRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> findAll() {
        Map<Long, Long> counts = enrolledCountsByCourse();

        return courseRepository.findAllWithTrainer().stream()
                .map(course -> CourseResponse.from(course, counts.getOrDefault(course.getId(), 0L)))
                .toList();
    }

    @Transactional(readOnly = true)
    public CourseResponse findById(Long id) {
        Course course = loadCourse(id);
        return CourseResponse.from(course, enrollmentRepository.countByCourseId(id));
    }

    @Transactional
    public CourseResponse create(CourseRequest request) {

        if (courseRepository.existsByTitleIgnoreCase(request.getTitle())) {
            throw new DuplicateResourceException("A course with this title already exists");
        }

        Trainer trainer = loadTrainer(request.getTrainerId());
        assertTrainerIsFree(trainer, null);

        Course course = new Course(request.getTitle(), request.getCapacity(), trainer);
        course.setDescription(trimOrNull(request.getDescription()));
        course.setDurationWeeks(request.getDurationWeeks());

        Course saved = courseRepository.save(course);

        notificationService.notifyCourseTrainer(
                saved,
                "تمّ إسناد كورس إليك",
                "أسند إليك مدير النظام كورس " + saved.getTitle()
                        + " بسعة " + saved.getCapacity() + " متدرّب.",
                "/trainer",
                NotificationType.COURSE_ASSIGNED);

        return CourseResponse.from(saved, 0L);
    }

    @Transactional
    public CourseResponse update(Long id, CourseRequest request) {

        Course course = loadCourse(id);

        if (!course.getTitle().equalsIgnoreCase(request.getTitle())
                && courseRepository.existsByTitleIgnoreCase(request.getTitle())) {
            throw new DuplicateResourceException("A course with this title already exists");
        }

        long enrolled = enrollmentRepository.countByCourseId(id);
        if (request.getCapacity() < enrolled) {
            throw new IllegalArgumentException(
                    "Capacity cannot be lower than the number of enrolled trainees (" + enrolled + ")");
        }

        Trainer newTrainer = loadTrainer(request.getTrainerId());
        if (!newTrainer.getId().equals(course.getTrainer().getId())) {
            assertTrainerIsFree(newTrainer, id);
        }

        course.setTitle(request.getTitle());
        course.setCapacity(request.getCapacity());
        course.setTrainer(newTrainer);
        course.setDescription(trimOrNull(request.getDescription()));
        course.setDurationWeeks(request.getDurationWeeks());

        return CourseResponse.from(course, enrolled);
    }

    /** يُستدعى من تدفّق حذف المدرّب لنقل الكورس إلى مدرّب بديل. */
    @Transactional
    public CourseResponse reassignTrainer(Long courseId, Long newTrainerId) {
        Course course = loadCourse(courseId);
        Trainer newTrainer = loadTrainer(newTrainerId);

        if (newTrainer.getId().equals(course.getTrainer().getId())) {
            throw new IllegalArgumentException("This course is already assigned to that trainer");
        }
        assertTrainerIsFree(newTrainer, courseId);

        course.setTrainer(newTrainer);

        notificationService.notifyCourseTrainer(
                course,
                "تمّ إسناد كورس إليك",
                "نُقل إليك كورس " + course.getTitle() + " ليصبح تحت إشرافك.",
                "/trainer",
                NotificationType.COURSE_ASSIGNED);

        return CourseResponse.from(course, enrollmentRepository.countByCourseId(courseId));
    }

    @Transactional
    public void delete(Long id) {
        Course course = loadCourse(id);

        long enrolled = enrollmentRepository.countByCourseId(id);
        if (enrolled > 0) {
            throw new IllegalArgumentException(
                    "This course cannot be deleted while " + enrolled + " trainee(s) are enrolled");
        }

        courseRepository.delete(course);
    }

    /**
     * الوصف والمدّة: يعدّلهما الأدمن عبر update، أو المدرّب المستلم للكورس
     * عبر هذه الدالة. مدرّب آخر يُرفض حتى لو مرّر معرّف الكورس صراحة —
     * دور TRAINER يمنع المتدربين لا الزملاء.
     */
    @Transactional
    public CourseResponse updateDetails(String trainerEmail, Long courseId,
                                        CourseDetailsRequest request) {
        Course course = loadCourse(courseId);

        Trainer trainer = trainerRepository.findByUserEmail(trainerEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No trainer profile is linked to this account"));

        if (!course.getTrainer().getId().equals(trainer.getId())) {
            throw new AccessDeniedException("This course is not assigned to you");
        }

        course.setDescription(trimOrNull(request.getDescription()));
        course.setDurationWeeks(request.getDurationWeeks());

        return CourseResponse.from(course, enrollmentRepository.countByCourseId(courseId));
    }

    private String trimOrNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    /**
     * قاعدة المشرف: كل مدرّب يستلم كورساً واحداً فقط.
     * excludeCourseId يسمح باستثناء الكورس الجاري تعديله من الفحص.
     */
    private void assertTrainerIsFree(Trainer trainer, Long excludeCourseId) {
        boolean busy = courseRepository.findAllByTrainerId(trainer.getId()).stream()
                .anyMatch(c -> excludeCourseId == null || !c.getId().equals(excludeCourseId));

        if (busy) {
            throw new IllegalStateException(
                    "Trainer " + trainer.getUser().getName()
                            + " is already assigned to a course. Each trainer may hold only one course.");
        }
    }

    private Map<Long, Long> enrolledCountsByCourse() {
        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : enrollmentRepository.countGroupedByCourse()) {
            counts.put((Long) row[0], (Long) row[1]);
        }
        return counts;
    }

    private Course loadCourse(Long id) {
        return courseRepository.findByIdWithTrainer(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + id));
    }

    private Trainer loadTrainer(Long trainerId) {
        return trainerRepository.findByIdWithUser(trainerId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found: " + trainerId));
    }
}
