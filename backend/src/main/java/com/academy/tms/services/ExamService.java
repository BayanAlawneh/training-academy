package com.academy.tms.services;

import com.academy.tms.dto.*;
import com.academy.tms.entities.*;
import com.academy.tms.entities.NotificationType;
import com.academy.tms.exception.ResourceNotFoundException;
import com.academy.tms.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * جانب المدرّب من الاختبارات: الإنشاء، النشر، وجدول العلامات.
 *
 * كل عملية تتحقّق من ملكية الاختبار لكورس هذا المدرّب — دور TRAINER
 * يمنع المتدربين لكنه لا يمنع مدرّباً من تعديل اختبار زميله.
 */
@Service
public class ExamService {

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final SubmissionRepository submissionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final TrainerRepository trainerRepository;
    private final CourseRepository courseRepository;
    private final NotificationService notificationService;

    public ExamService(ExamRepository examRepository,
                       QuestionRepository questionRepository,
                       SubmissionRepository submissionRepository,
                       EnrollmentRepository enrollmentRepository,
                       TrainerRepository trainerRepository,
                       CourseRepository courseRepository,
                       NotificationService notificationService) {
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.submissionRepository = submissionRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.trainerRepository = trainerRepository;
        this.courseRepository = courseRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<ExamResponse> myExams(String email) {
        Course course = loadMyCourse(email);

        return examRepository.findAllByCourseId(course.getId()).stream()
                .map(exam -> ExamResponse.summary(
                        exam,
                        (int) questionRepository.countByExamId(exam.getId()),
                        submissionRepository.findAllByExamIdWithTrainee(exam.getId()).size()))
                .toList();
    }

    @Transactional(readOnly = true)
    public ExamResponse examDetail(String email, Long examId) {
        Exam exam = loadOwnedExam(email, examId);

        List<QuestionResponse> questions = questionRepository
                .findAllByExamIdWithOptions(examId).stream()
                .map(QuestionResponse::forTrainer)
                .toList();

        return new ExamResponse(
                exam.getId(), exam.getCourse().getId(), exam.getCourse().getTitle(),
                exam.getTitle(), exam.getDescription(), exam.getTotalMarks(),
                exam.getOpensAt(), exam.getClosesAt(), exam.isPublished(),
                questions.size(),
                submissionRepository.findAllByExamIdWithTrainee(examId).size(),
                questions);
    }

    @Transactional
    public ExamResponse create(String email, ExamRequest request) {
        Course course = loadMyCourse(email);
        validateWindow(request.getOpensAt(), request.getClosesAt());
        validateQuestions(request);

        int total = request.getQuestions().stream()
                .mapToInt(ExamRequest.QuestionRequest::getMarks)
                .sum();

        Exam exam = new Exam(course, request.getTitle(), request.getDescription(),
                total, request.getOpensAt(), request.getClosesAt());

        buildQuestions(exam, request);

        Exam saved = examRepository.save(exam);
        return ExamResponse.summary(saved, saved.getQuestions().size(), 0);
    }

    /**
     * التعديل يستبدل الأسئلة بالكامل (orphanRemoval).
     * ممنوع بعد أول تسليم — تغيير الأسئلة يُبطل العلامات المحتسبة.
     */
    @Transactional
    public ExamResponse update(String email, Long examId, ExamRequest request) {
        Exam exam = loadOwnedExam(email, examId);

        if (!submissionRepository.findAllByExamIdWithTrainee(examId).isEmpty()) {
            throw new IllegalStateException(
                    "This exam already has submissions and can no longer be edited");
        }

        validateWindow(request.getOpensAt(), request.getClosesAt());
        validateQuestions(request);

        exam.setTitle(request.getTitle());
        exam.setDescription(request.getDescription());
        exam.setOpensAt(request.getOpensAt());
        exam.setClosesAt(request.getClosesAt());
        exam.setTotalMarks(request.getQuestions().stream()
                .mapToInt(ExamRequest.QuestionRequest::getMarks).sum());

        exam.getQuestions().clear();
        buildQuestions(exam, request);

        return ExamResponse.summary(exam, exam.getQuestions().size(), 0);
    }

    @Transactional
    public ExamResponse setPublished(String email, Long examId, boolean published) {
        Exam exam = loadOwnedExam(email, examId);

        if (published && questionRepository.countByExamId(examId) == 0) {
            throw new IllegalStateException("Cannot publish an exam with no questions");
        }

        exam.setPublished(published);

        if (published) {
            notificationService.notifyCourseTrainees(
                    exam.getCourse().getId(),
                    "اختبار جديد: " + exam.getTitle(),
                    "كورس " + exam.getCourse().getTitle() + " — العلامة الكاملة "
                            + exam.getTotalMarks() + ". تأكّد من التقديم داخل الوقت المحدّد.",
                    "/trainee/exams",
                    NotificationType.EXAM_PUBLISHED);
        }

        return ExamResponse.summary(exam,
                (int) questionRepository.countByExamId(examId),
                submissionRepository.findAllByExamIdWithTrainee(examId).size());
    }

    @Transactional
    public void delete(String email, Long examId) {
        Exam exam = loadOwnedExam(email, examId);

        if (!submissionRepository.findAllByExamIdWithTrainee(examId).isEmpty()) {
            throw new IllegalStateException(
                    "This exam has submissions and cannot be deleted");
        }

        examRepository.delete(exam);
    }

    /**
     * جدول العلامات: كل متدربي الكورس، لا المسلِّمون فقط.
     * من لم يسلّم بعد إغلاق النافذة يظهر بصفر — تُحتسب لحظة العرض
     * بلا مهمة مجدولة تكتب أصفاراً في القاعدة.
     */
    @Transactional(readOnly = true)
    public List<GradeRowResponse> grades(String email, Long examId) {
        Exam exam = loadOwnedExam(email, examId);
        List<Submission> submissions = submissionRepository.findAllByExamIdWithTrainee(examId);

        return enrollmentRepository.findAllByCourseIdWithDetails(exam.getCourse().getId()).stream()
                .map(enrollment -> {
                    Trainee trainee = enrollment.getTrainee();

                    Submission match = submissions.stream()
                            .filter(s -> s.getTrainee().getId().equals(trainee.getId()))
                            .findFirst().orElse(null);

                    if (match != null) {
                        return new GradeRowResponse(trainee.getId(),
                                trainee.getUser().getName(), trainee.getUser().getEmail(),
                                match.getScore(), exam.getTotalMarks(), "SUBMITTED",
                                match.getSubmittedAt());
                    }

                    String state = exam.hasClosed() ? "MISSED" : "PENDING";
                    Integer score = exam.hasClosed() ? 0 : null;

                    return new GradeRowResponse(trainee.getId(),
                            trainee.getUser().getName(), trainee.getUser().getEmail(),
                            score, exam.getTotalMarks(), state, null);
                })
                .toList();
    }

    // ---------- مساعدات ----------

    private void buildQuestions(Exam exam, ExamRequest request) {
        int position = 1;

        for (ExamRequest.QuestionRequest qr : request.getQuestions()) {
            QuestionType type = QuestionType.MCQ;
            if (qr.getType() != null && !qr.getType().isBlank()) {
                try {
                    type = QuestionType.valueOf(qr.getType());
                } catch (IllegalArgumentException ex) {
                    throw new IllegalArgumentException("Unknown question type: " + qr.getType());
                }
            }

            Question question = new Question(exam, qr.getText(), type, qr.getMarks(), position++);

            int optionPosition = 1;
            List<QuestionOption> options = new ArrayList<>();
            for (ExamRequest.OptionRequest or : qr.getOptions()) {
                if (type == QuestionType.MATCHING) {
                    options.add(new QuestionOption(
                            question, or.getText(), or.getMatchText(), optionPosition++));
                } else {
                    options.add(new QuestionOption(
                            question, or.getText(), or.isCorrect(), optionPosition++));
                }
            }
            question.getOptions().addAll(options);

            exam.getQuestions().add(question);
        }
    }

    private void validateWindow(LocalDateTime opensAt, LocalDateTime closesAt) {
        if (opensAt != null && closesAt != null && !closesAt.isAfter(opensAt)) {
            throw new IllegalArgumentException("Closing time must be after opening time");
        }
    }

    /**
     * قواعد التحقّق تختلف بحسب النوع:
     *   MCQ / TRUE_FALSE — خيارَان على الأقل، وإجابة صحيحة واحدة بالضبط.
     *   MATCHING        — زوجَان على الأقل، ولكل زوج طرفان مكتوبان.
     * التحقّق في الخادم لا في الواجهة، فسؤال ناقص لا يمكن حفظه بأي طريق.
     */
    private void validateQuestions(ExamRequest request) {
        int index = 1;

        for (ExamRequest.QuestionRequest qr : request.getQuestions()) {

            QuestionType type = QuestionType.MCQ;
            if (qr.getType() != null && !qr.getType().isBlank()) {
                try {
                    type = QuestionType.valueOf(qr.getType());
                } catch (IllegalArgumentException ex) {
                    throw new IllegalArgumentException("Unknown question type: " + qr.getType());
                }
            }

            if (qr.getOptions() == null || qr.getOptions().size() < 2) {
                throw new IllegalArgumentException(
                        type == QuestionType.MATCHING
                                ? "Question " + index + " needs at least two pairs"
                                : "Question " + index + " needs at least two options");
            }

            if (type == QuestionType.MATCHING) {
                int pair = 1;
                for (ExamRequest.OptionRequest or : qr.getOptions()) {
                    if (or.getMatchText() == null || or.getMatchText().isBlank()) {
                        throw new IllegalArgumentException(
                                "Question " + index + ", pair " + pair + " is missing its right-hand item");
                    }
                    pair++;
                }
            } else {
                long correct = qr.getOptions().stream()
                        .filter(ExamRequest.OptionRequest::isCorrect).count();
                if (correct != 1) {
                    throw new IllegalArgumentException(
                            "Question " + index + " must have exactly one correct option");
                }
            }

            index++;
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

    private Exam loadOwnedExam(String email, Long examId) {
        Exam exam = examRepository.findByIdWithCourse(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found: " + examId));

        Trainer trainer = loadTrainer(email);
        if (!exam.getCourse().getTrainer().getId().equals(trainer.getId())) {
            throw new AccessDeniedException("This exam does not belong to your course");
        }
        return exam;
    }
}
