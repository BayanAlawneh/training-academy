package com.academy.tms.services;

import com.academy.tms.dto.CourseRequest;
import com.academy.tms.dto.CourseResponse;
import com.academy.tms.entities.Course;
import com.academy.tms.entities.Trainer;
import com.academy.tms.exception.DuplicateResourceException;
import com.academy.tms.exception.ResourceNotFoundException;
import com.academy.tms.repository.CourseRepository;
import com.academy.tms.repository.EnrollmentRepository;
import com.academy.tms.repository.TrainerRepository;
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

    public CourseService(CourseRepository courseRepository,
                         TrainerRepository trainerRepository,
                         EnrollmentRepository enrollmentRepository) {
        this.courseRepository = courseRepository;
        this.trainerRepository = trainerRepository;
        this.enrollmentRepository = enrollmentRepository;
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
        Course course = new Course(request.getTitle(), request.getCapacity(), trainer);

        return CourseResponse.from(courseRepository.save(course), 0L);
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

        course.setTitle(request.getTitle());
        course.setCapacity(request.getCapacity());
        course.setTrainer(loadTrainer(request.getTrainerId()));

        return CourseResponse.from(course, enrolled);
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
