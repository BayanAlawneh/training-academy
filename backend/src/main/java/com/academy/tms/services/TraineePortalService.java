package com.academy.tms.services;

import com.academy.tms.dto.MyCourseResponse;
import com.academy.tms.dto.MySummaryResponse;
import com.academy.tms.entities.Trainee;
import com.academy.tms.exception.ResourceNotFoundException;
import com.academy.tms.repository.EnrollmentRepository;
import com.academy.tms.repository.TraineeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * خدمة "بوابة المتدرّب" — كل ما يخصّ المتدرّب عن نفسه.
 *
 * الفصل عن TraineeService مقصود: تلك خدمة إدارية (الأدمن يدير المتدربين)،
 * وهذه خدمة ذاتية (المتدرّب يقرأ بياناته هو). الفصل يمنع تسرّب عمليات
 * إدارية إلى مسار /api/trainee، ويجعل مراجعة الصلاحيات أوضح.
 */
@Service
public class TraineePortalService {

    private final TraineeRepository traineeRepository;
    private final EnrollmentRepository enrollmentRepository;

    public TraineePortalService(TraineeRepository traineeRepository,
                                EnrollmentRepository enrollmentRepository) {
        this.traineeRepository = traineeRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    /**
     * الكورسات التي سجّل الأدمن هذا المتدرّب فيها.
     * البريد يأتي من التوكن، لا من الطلب — فلا يستطيع متدرّب قراءة بيانات غيره
     * حتى لو عدّل عنوان الصفحة.
     */
    @Transactional(readOnly = true)
    public List<MyCourseResponse> myCourses(String email) {
        Trainee trainee = loadByEmail(email);

        return enrollmentRepository.findAllByTraineeIdWithDetails(trainee.getId()).stream()
                .map(MyCourseResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public MySummaryResponse mySummary(String email) {
        Trainee trainee = loadByEmail(email);

        return new MySummaryResponse(
                trainee.getUser().getName(),
                trainee.getUser().getEmail(),
                trainee.getEnrollmentDate(),
                enrollmentRepository.countByTraineeId(trainee.getId())
        );
    }

    private Trainee loadByEmail(String email) {
        return traineeRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No trainee profile is linked to this account"));
    }
}
