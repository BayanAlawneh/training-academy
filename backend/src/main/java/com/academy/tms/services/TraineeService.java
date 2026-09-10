package com.academy.tms.services;

import com.academy.tms.dto.TraineeRequest;
import com.academy.tms.dto.TraineeResponse;
import com.academy.tms.entities.Role;
import com.academy.tms.entities.RoleName;
import com.academy.tms.entities.Trainee;
import com.academy.tms.entities.User;
import com.academy.tms.exception.DuplicateResourceException;
import com.academy.tms.exception.ResourceNotFoundException;
import com.academy.tms.repository.AttendanceRepository;
import com.academy.tms.repository.NotificationRepository;
import com.academy.tms.repository.SubmissionRepository;
import com.academy.tms.repository.EnrollmentRepository;
import com.academy.tms.repository.RoleRepository;
import com.academy.tms.repository.TraineeRepository;
import com.academy.tms.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Service
public class TraineeService {

    private final TraineeRepository traineeRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final SubmissionRepository submissionRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    public TraineeService(TraineeRepository traineeRepository,
                          UserRepository userRepository,
                          RoleRepository roleRepository,
                          EnrollmentRepository enrollmentRepository,
                          AttendanceRepository attendanceRepository,
                          SubmissionRepository submissionRepository,
                          NotificationRepository notificationRepository,
                          PasswordEncoder passwordEncoder) {
        this.traineeRepository = traineeRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.attendanceRepository = attendanceRepository;
        this.submissionRepository = submissionRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<TraineeResponse> findAll() {
        return traineeRepository.findAllWithUser().stream()
                .map(TraineeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TraineeResponse findById(Long id) {
        return TraineeResponse.from(loadTrainee(id));
    }

    @Transactional
    public TraineeResponse create(TraineeRequest request) {

        if (!StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("Password is required for a new trainee");
        }
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException("This email is already registered");
        }
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new DuplicateResourceException("This username is already taken");
        }

        Role traineeRole = roleRepository.findByRoleName(RoleName.TRAINEE)
                .orElseThrow(() -> new IllegalStateException("TRAINEE role not found"));

        User user = new User(
                request.getUsername(),
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getDateOfBirth(),
                traineeRole
        );

        LocalDate enrollmentDate = request.getEnrollmentDate() != null
                ? request.getEnrollmentDate()
                : LocalDate.now();

        Trainee trainee = new Trainee(userRepository.save(user), enrollmentDate);
        return TraineeResponse.from(traineeRepository.save(trainee));
    }

    @Transactional
    public TraineeResponse update(Long id, TraineeRequest request) {

        Trainee trainee = loadTrainee(id);
        User user = trainee.getUser();

        if (!user.getEmail().equalsIgnoreCase(request.getEmail())
                && userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException("This email is already registered");
        }
        if (!user.getUsername().equalsIgnoreCase(request.getUsername())
                && userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new DuplicateResourceException("This username is already taken");
        }

        user.setUsername(request.getUsername());
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setDateOfBirth(request.getDateOfBirth());

        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getEnrollmentDate() != null) {
            trainee.setEnrollmentDate(request.getEnrollmentDate());
        }

        return TraineeResponse.from(trainee);
    }

    /**
     * كم كورساً مسجَّل فيه هذا المتدرّب — يستدعيه الفرونت اند قبل الحذف
     * ليعرض رسالة تأكيد دقيقة بدل رسالة عامة.
     */
    @Transactional(readOnly = true)
    public long enrollmentCount(Long id) {
        loadTrainee(id);
        return enrollmentRepository.countByTraineeId(id);
    }

    /**
     * الحذف بالترتيب الصحيح للمفاتيح الأجنبية:
     *   الإجابات ← التسليمات ← الحضور ← التسجيلات ← الملف ← الإشعارات ← الحساب
     *
     * سابقاً كانت التسجيلات تُترك، فيرفض PostgreSQL حذف المتدرّب
     * ويُرمى DataIntegrityViolationException بلا معالجة، فيتحوّل الردّ
     * إلى 401 ويُطرد الأدمن من النظام.
     *
     * flush() بعد كل خطوة يفرض على Hibernate تنفيذ الحذف بالترتيب المكتوب
     * بدل ترتيبه الداخلي، وهذا يمنع فشل حذف (trainee ← user) أيضاً.
     */
    @Transactional
    public void delete(Long id) {
        Trainee trainee = loadTrainee(id);
        User user = trainee.getUser();

        // الترتيب مهم: الإجابات تشير إلى التسليمات، والتسليمات والحضور
        // والتسجيلات كلها تشير إلى المتدرّب. أي واحد يُترك يُفشل الحذف.
        submissionRepository.deleteAnswersByTraineeId(id);
        submissionRepository.flush();

        submissionRepository.deleteAllByTraineeId(id);
        submissionRepository.flush();

        attendanceRepository.deleteAllByTraineeId(id);
        attendanceRepository.flush();

        enrollmentRepository.deleteAllByTraineeId(id);
        enrollmentRepository.flush();

        traineeRepository.delete(trainee);
        traineeRepository.flush();

        // الإشعارات تشير إلى المستخدم مباشرة، فتُحذف قبله.
        notificationRepository.deleteAllByUserId(user.getId());
        notificationRepository.flush();

        userRepository.delete(user);
        userRepository.flush();
    }

    private Trainee loadTrainee(Long id) {
        return traineeRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee not found: " + id));
    }
}
