package com.academy.tms.repository;

import com.academy.tms.entities.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    @Query("select t from Trainer t join fetch t.user u join fetch u.role order by t.id")
    List<Trainer> findAllWithUser();

    @Query("select t from Trainer t join fetch t.user u join fetch u.role where t.id = :id")
    Optional<Trainer> findByIdWithUser(@Param("id") Long id);

    /** يربط المستخدم المسجَّل دخوله بملفّه كمدرّب — الجسر بين التوكن والبيانات. */
    @Query("select t from Trainer t join fetch t.user u join fetch u.role where lower(u.email) = lower(:email)")
    Optional<Trainer> findByUserEmail(@Param("email") String email);
}
