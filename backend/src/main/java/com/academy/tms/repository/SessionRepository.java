package com.academy.tms.repository;

import com.academy.tms.entities.TrainingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<TrainingSession, Long> {

    @Query("""
           select s from TrainingSession s
           join fetch s.course c
           where c.id = :courseId
           order by s.sessionDate, s.startTime
           """)
    List<TrainingSession> findAllByCourseId(@Param("courseId") Long courseId);

    @Query("""
           select s from TrainingSession s
           join fetch s.course c
           join fetch c.trainer t
           join fetch t.user
           where s.id = :id
           """)
    Optional<TrainingSession> findByIdWithCourse(@Param("id") Long id);

    /** جلسات كل الكورسات التي سُجّل فيها متدرّب — أساس صفحة "حضوري". */
    @Query("""
           select s from TrainingSession s
           join fetch s.course c
           join fetch c.trainer t
           join fetch t.user
           where c.id in (select e.course.id from Enrollment e where e.trainee.id = :traineeId)
           order by s.sessionDate, s.startTime
           """)
    List<TrainingSession> findAllForTrainee(@Param("traineeId") Long traineeId);

    long countByCourseId(Long courseId);
}
