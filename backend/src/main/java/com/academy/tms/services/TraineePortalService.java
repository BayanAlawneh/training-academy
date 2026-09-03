package com.academy.tms.services;

import com.academy.tms.dto.MyAttendanceResponse;
import com.academy.tms.dto.MyCourseResponse;
import com.academy.tms.dto.MySummaryResponse;
import com.academy.tms.entities.Attendance;
import com.academy.tms.entities.Trainee;
import com.academy.tms.entities.TrainingSession;
import com.academy.tms.exception.ResourceNotFoundException;
import com.academy.tms.repository.AttendanceRepository;
import com.academy.tms.repository.EnrollmentRepository;
import com.academy.tms.repository.SessionRepository;
import com.academy.tms.repository.TraineeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TraineePortalService {

    private final TraineeRepository traineeRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SessionRepository sessionRepository;
    private final AttendanceRepository attendanceRepository;

    public TraineePortalService(TraineeRepository traineeRepository,
                                EnrollmentRepository enrollmentRepository,
                                SessionRepository sessionRepository,
                                AttendanceRepository attendanceRepository) {
        this.traineeRepository = traineeRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.sessionRepository = sessionRepository;
        this.attendanceRepository = attendanceRepository;
    }

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

    /**
     * صفحة "حضوري": كل جلسات كورساتي، وحالتي في كل جلسة.
     *
     * نبدأ من الجلسات لا من سجلّات الحضور، لأن الجلسة التي لم يسجّل المدرّب
     * حضورها بعد يجب أن تظهر للمتدرّب كموعد قادم — لا أن تختفي.
     */
    @Transactional(readOnly = true)
    public List<MyAttendanceResponse> myAttendance(String email) {
        Trainee trainee = loadByEmail(email);

        Map<Long, Attendance> bySession = attendanceRepository
                .findAllByTraineeId(trainee.getId()).stream()
                .collect(Collectors.toMap(
                        a -> a.getSession().getId(),
                        Function.identity(),
                        (first, second) -> first));

        return sessionRepository.findAllForTrainee(trainee.getId()).stream()
                .map(session -> toRow(session, bySession.get(session.getId())))
                .toList();
    }

    private MyAttendanceResponse toRow(TrainingSession session, Attendance attendance) {
        return new MyAttendanceResponse(
                session.getId(),
                session.getCourse().getId(),
                session.getCourse().getTitle(),
                session.getTitle(),
                session.getSessionDate(),
                session.getStartTime(),
                session.getEndTime(),
                session.getMeetingLink(),
                session.getStatus().name(),
                attendance != null ? attendance.getStatus().name() : "NOT_RECORDED",
                attendance != null ? attendance.getNote() : null
        );
    }

    private Trainee loadByEmail(String email) {
        return traineeRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No trainee profile is linked to this account"));
    }
}
