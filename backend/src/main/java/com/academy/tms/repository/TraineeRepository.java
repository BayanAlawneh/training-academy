package com.academy.tms.repository;

import com.academy.tms.entities.Trainee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TraineeRepository extends JpaRepository<Trainee, Long> {

    @Query("select t from Trainee t join fetch t.user u join fetch u.role order by t.id")
    List<Trainee> findAllWithUser();

    @Query("select t from Trainee t join fetch t.user u join fetch u.role where t.id = :id")
    Optional<Trainee> findByIdWithUser(@Param("id") Long id);

    /**
     * يربط المستخدم المسجَّل دخوله بملفّه كمتدرّب.
     * التوكن يحمل البريد فقط، فهذا هو الجسر بين الهوية والبيانات.
     */
    @Query("select t from Trainee t join fetch t.user u join fetch u.role where lower(u.email) = lower(:email)")
    Optional<Trainee> findByUserEmail(@Param("email") String email);
}
