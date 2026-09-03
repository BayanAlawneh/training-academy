package com.academy.tms.repository;

import com.academy.tms.entities.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    @Query("""
           select a from Attendance a
           join fetch a.trainee t
           join fetch t.user
           where a.session.id = :sessionId
           order by a.id
           """)
    List<Attendance> findAllBySessionId(@Param("sessionId") Long sessionId);

    @Query("""
           select a from Attendance a
           join fetch a.session s
           join fetch s.course
           where a.trainee.id = :traineeId
           order by s.sessionDate, s.startTime
           """)
    List<Attendance> findAllByTraineeId(@Param("traineeId") Long traineeId);

    Optional<Attendance> findBySessionIdAndTraineeId(Long sessionId, Long traineeId);

    /** تنظيف قبل حذف متدرّب — يمنع خرق المفتاح الأجنبي. */
    @Modifying
    @Query("delete from Attendance a where a.trainee.id = :traineeId")
    int deleteAllByTraineeId(@Param("traineeId") Long traineeId);

    @Modifying
    @Query("delete from Attendance a where a.session.id = :sessionId")
    int deleteAllBySessionId(@Param("sessionId") Long sessionId);
}
