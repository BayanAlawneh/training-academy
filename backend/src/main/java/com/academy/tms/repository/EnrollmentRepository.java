package com.academy.tms.repository;

import com.academy.tms.entities.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    long countByCourseId(Long courseId);

    long countByTraineeId(Long traineeId);

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

    /** كورسات متدرّب معيّن — أساس صفحة "كورساتي". */
    @Query("""
           select e from Enrollment e
           join fetch e.course c
           join fetch c.trainer tr
           join fetch tr.user
           where e.trainee.id = :traineeId
           order by e.id
           """)
    List<Enrollment> findAllByTraineeIdWithDetails(@Param("traineeId") Long traineeId);

    @Query("select e.course.id, count(e) from Enrollment e group by e.course.id")
    List<Object[]> countGroupedByCourse();

    /** حذف تسجيلات متدرّب قبل حذفه — يمنع خرق قيد المفتاح الأجنبي. */
    @Modifying
    @Query("delete from Enrollment e where e.trainee.id = :traineeId")
    int deleteAllByTraineeId(@Param("traineeId") Long traineeId);
}
