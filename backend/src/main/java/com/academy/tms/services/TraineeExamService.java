package com.academy.tms.services;

import com.academy.tms.dto.*;
import com.academy.tms.entities.*;
import com.academy.tms.exception.ResourceNotFoundException;
import com.academy.tms.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * جانب المتدرّب: عرض اختباراته، فتح الورقة، والتسليم مع تصحيح فوري.
 *
 * النافذة الزمنية تُفرض هنا في الخادم لا في الواجهة. إخفاء زر البدء
 * في المتصفح ليس حماية — أي شخص يستطيع استدعاء الـ endpoint مباشرة.
 */
@Service
public class TraineeExamService {

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final SubmissionRepository submissionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final TraineeRepository traineeRepository;
    private final NotificationService notificationService;

    public TraineeExamService(ExamRepository examRepository,
                              QuestionRepository questionRepository,
                              SubmissionRepository submissionRepository,
                              EnrollmentRepository enrollmentRepository,
                              TraineeRepository traineeRepository,
                              NotificationService notificationService) {
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.submissionRepository = submissionRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.traineeRepository = traineeRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<MyExamResponse> myExams(String email) {
        Trainee trainee = loadTrainee(email);
        List<Submission> mine = submissionRepository.findAllByTraineeId(trainee.getId());

        return examRepository.findPublishedForTrainee(trainee.getId()).stream()
                .map(exam -> {
                    Submission submission = mine.stream()
                            .filter(s -> s.getExam().getId().equals(exam.getId()))
                            .findFirst().orElse(null);

                    return toRow(exam, submission,
                            (int) questionRepository.countByExamId(exam.getId()));
                })
                .toList();
    }

    /** يفتح ورقة الاختبار — بعد فرض النافذة ومنع التكرار. */
    @Transactional(readOnly = true)
    public ExamPaperResponse openPaper(String email, Long examId) {
        Trainee trainee = loadTrainee(email);
        Exam exam = loadAccessibleExam(trainee, examId);

        if (submissionRepository.existsByExamIdAndTraineeId(examId, trainee.getId())) {
            throw new IllegalStateException("You have already submitted this exam");
        }
        assertWindowOpen(exam);

        List<QuestionResponse> questions = questionRepository
                .findAllByExamIdWithOptions(examId).stream()
                .map(QuestionResponse::forTrainee)
                .toList();

        return new ExamPaperResponse(exam.getId(), exam.getTitle(), exam.getDescription(),
                exam.getTotalMarks(), exam.getClosesAt().toString(), questions);
    }

    /**
     * التسليم والتصحيح في معاملة واحدة.
     * السؤال بلا إجابة يأخذ صفراً ولا يوقف التسليم.
     */
    @Transactional
    public SubmissionResultResponse submit(String email, Long examId, SubmitExamRequest request) {
        Trainee trainee = loadTrainee(email);
        Exam exam = loadAccessibleExam(trainee, examId);

        if (submissionRepository.existsByExamIdAndTraineeId(examId, trainee.getId())) {
            throw new IllegalStateException("You have already submitted this exam");
        }
        assertWindowOpen(exam);

        List<Question> questions = questionRepository.findAllByExamIdWithOptions(examId);

        Submission submission = new Submission(exam, trainee, 0, LocalDateTime.now());

        int score = 0;
        int correctCount = 0;
        List<Answer> answers = new ArrayList<>();

        for (Question question : questions) {
            Long chosenId = request.getAnswers() == null ? null :
                    request.getAnswers().stream()
                            .filter(a -> question.getId().equals(a.getQuestionId()))
                            .map(SubmitExamRequest.AnswerEntry::getSelectedOptionId)
                            .findFirst().orElse(null);

            QuestionOption chosen = null;
            if (chosenId != null) {
                chosen = question.getOptions().stream()
                        .filter(o -> o.getId().equals(chosenId))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Option " + chosenId + " does not belong to question " + question.getId()));
            }

            int awarded = (chosen != null && chosen.isCorrect()) ? question.getMarks() : 0;
            if (awarded > 0) correctCount++;
            score += awarded;

            answers.add(new Answer(submission, question, chosen, awarded));
        }

        submission.setScore(score);
        submission.getAnswers().addAll(answers);
        submissionRepository.save(submission);

        notificationService.notifyCourseTrainer(
                exam.getCourse(),
                "تسليم اختبار: " + exam.getTitle(),
                trainee.getUser().getName() + " سلّم الاختبار — العلامة "
                        + score + " من " + exam.getTotalMarks() + ".",
                "/trainer/exams",
                NotificationType.EXAM_SUBMITTED);

        return new SubmissionResultResponse(exam.getId(), exam.getTitle(), score,
                exam.getTotalMarks(), correctCount, questions.size());
    }

    // ---------- مساعدات ----------

    private MyExamResponse toRow(Exam exam, Submission submission, int questionCount) {
        String state;
        Integer score = null;
        LocalDateTime submittedAt = null;

        if (submission != null) {
            state = "SUBMITTED";
            score = submission.getScore();
            submittedAt = submission.getSubmittedAt();
        } else if (exam.hasClosed()) {
            // الصفر يُحتسب عند العرض، فلا حاجة لمهمة مجدولة تكتبه.
            state = "MISSED";
            score = 0;
        } else if (exam.isOpenNow()) {
            state = "OPEN";
        } else {
            state = "UPCOMING";
        }

        return new MyExamResponse(exam.getId(), exam.getCourse().getId(),
                exam.getCourse().getTitle(), exam.getTitle(), exam.getDescription(),
                exam.getTotalMarks(), exam.getOpensAt(), exam.getClosesAt(),
                questionCount, state, score, submittedAt);
    }

    private void assertWindowOpen(Exam exam) {
        LocalDateTime now = LocalDateTime.now();

        if (!exam.isPublished()) {
            throw new AccessDeniedException("This exam is not available");
        }
        if (now.isBefore(exam.getOpensAt())) {
            throw new IllegalStateException("This exam has not opened yet");
        }
        if (now.isAfter(exam.getClosesAt())) {
            throw new IllegalStateException("The submission window for this exam has closed");
        }
    }

    /** يمنع متدرّباً من فتح اختبار كورس ليس مسجّلاً فيه. */
    private Exam loadAccessibleExam(Trainee trainee, Long examId) {
        Exam exam = examRepository.findByIdWithCourse(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found: " + examId));

        if (!enrollmentRepository.existsByTraineeIdAndCourseId(
                trainee.getId(), exam.getCourse().getId())) {
            throw new AccessDeniedException("You are not enrolled in this course");
        }
        return exam;
    }

    private Trainee loadTrainee(String email) {
        return traineeRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No trainee profile is linked to this account"));
    }
}
