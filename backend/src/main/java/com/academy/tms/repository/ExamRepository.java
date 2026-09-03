package com.academy.tms.repository;

import com.academy.tms.entities.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {

    @Query("select e from Exam e join fetch e.course where e.course.id = :courseId order by e.opensAt")
    List<Exam> findAllByCourseId(@Param("courseId") Long courseId);

    @Query("""
           select e from Exam e
           join fetch e.course c
           join fetch c.trainer t
           join fetch t.user
           where e.id = :id
           """)
    Optional<Exam> findByIdWithCourse(@Param("id") Long id);

    /** اختبارات كل الكورسات التي سُجّل فيها متدرّب — المنشورة فقط. */
    @Query("""
           select e from Exam e
           join fetch e.course c
           where e.published = true
             and c.id in (select en.course.id from Enrollment en where en.trainee.id = :traineeId)
           order by e.opensAt
           """)
    List<Exam> findPublishedForTrainee(@Param("traineeId") Long traineeId);

    long countByCourseId(Long courseId);
}
