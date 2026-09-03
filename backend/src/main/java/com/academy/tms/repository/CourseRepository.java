package com.academy.tms.repository;

import com.academy.tms.entities.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    @Query("select c from Course c join fetch c.trainer t join fetch t.user order by c.id")
    List<Course> findAllWithTrainer();

    @Query("select c from Course c join fetch c.trainer t join fetch t.user where c.id = :id")
    Optional<Course> findByIdWithTrainer(@Param("id") Long id);

    boolean existsByTitleIgnoreCase(String title);

    List<Course> findAllByTrainerId(Long trainerId);

    /** أساس قاعدة "كل مدرّب يستلم كورساً واحداً فقط". */
    boolean existsByTrainerId(Long trainerId);

    /** معرّفات كل المدربين المشغولين بكورس — لحساب المدربين المتاحين. */
    @Query("select distinct c.trainer.id from Course c")
    List<Long> findAllAssignedTrainerIds();

    /** كورس المدرّب الحالي — أساس لوحة المدرّب. */
    @Query("select c from Course c join fetch c.trainer t join fetch t.user u where u.id = :userId")
    List<Course> findAllByTrainerUserId(@Param("userId") Long userId);
}
