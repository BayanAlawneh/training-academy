package com.academy.tms.repository;

import com.academy.tms.entities.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    long countByCourseId(Long courseId);

    boolean existsByTraineeIdAndCourseId(Long traineeId, Long courseId);

    @Query("""
           select e from Enrollment e
           join fetch e.trainee t
           join fetch t.user
           join fetch e.course c
           where c.id = :courseId
           order by e.id
           """)
    List<Enrollment> findAllByCourseIdWithDetails(@Param("courseId") Long courseId);

    @Query("select e.course.id, count(e) from Enrollment e group by e.course.id")
    List<Object[]> countGroupedByCourse();
}
