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
}
