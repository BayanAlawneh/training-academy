package com.academy.tms.services;

import com.academy.tms.dto.EnrollmentRequest;
import com.academy.tms.dto.EnrollmentResponse;
import com.academy.tms.entities.Course;
import com.academy.tms.entities.Enrollment;
import com.academy.tms.entities.Trainee;
import com.academy.tms.exception.DuplicateResourceException;
import com.academy.tms.exception.ResourceNotFoundException;
import com.academy.tms.repository.CourseRepository;
import com.academy.tms.repository.EnrollmentRepository;
import com.academy.tms.entities.NotificationType;
import com.academy.tms.repository.TraineeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final TraineeRepository traineeRepository;
    private final NotificationService notificationService;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             CourseRepository courseRepository,
                             TraineeRepository traineeRepository,
                             NotificationService notificationService) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.traineeRepository = traineeRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> findByCourse(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found: " + courseId);
        }

        return enrollmentRepository.findAllByCourseIdWithDetails(courseId).stream()
                .map(EnrollmentResponse::from)
                .toList();
    }

    @Transactional
    public EnrollmentResponse enrol(EnrollmentRequest request) {

        Course course = courseRepository.findByIdWithTrainer(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course not found: " + request.getCourseId()));

        Trainee trainee = traineeRepository.findByIdWithUser(request.getTraineeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Trainee not found: " + request.getTraineeId()));

        if (enrollmentRepository.existsByTraineeIdAndCourseId(trainee.getId(), course.getId())) {
            throw new DuplicateResourceException(
                    "This trainee is already enrolled in this course");
        }

        long enrolled = enrollmentRepository.countByCourseId(course.getId());
        if (enrolled >= course.getCapacity()) {
            throw new IllegalArgumentException(
                    "This course is full (" + enrolled + " of " + course.getCapacity() + ")");
        }

        Enrollment enrollment = new Enrollment(trainee, course, LocalDate.now());
        Enrollment saved = enrollmentRepository.save(enrollment);

        notificationService.notifyUser(
                trainee.getUser(),
                "تمّ تسجيلك في كورس جديد",
                "سجّلك مدير النظام في كورس " + course.getTitle()
                        + " مع المدرّب " + course.getTrainer().getUser().getName() + ".",
                "/trainee/courses",
                NotificationType.ENROLLED);

        notificationService.notifyCourseTrainer(
                course,
                "متدرّب جديد في كورسك",
                "تمّ تسجيل " + trainee.getUser().getName() + " في كورس " + course.getTitle() + ".",
                "/trainer/sessions",
                NotificationType.TRAINEE_ENROLLED);

        return EnrollmentResponse.from(saved);
    }

    @Transactional
    public void remove(Long id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found: " + id));
        Course course = enrollment.getCourse();
        String traineeName = enrollment.getTrainee().getUser().getName();

        enrollmentRepository.delete(enrollment);

        notificationService.notifyCourseTrainer(
                course,
                "إلغاء تسجيل متدرّب",
                "تمّ إلغاء تسجيل " + traineeName + " من كورس " + course.getTitle() + ".",
                "/trainer/sessions",
                NotificationType.TRAINEE_REMOVED);
    }
}
