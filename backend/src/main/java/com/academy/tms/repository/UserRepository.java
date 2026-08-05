package com.academy.tms.repository;

import com.academy.tms.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select u from User u join fetch u.role where lower(u.email) = lower(:email)")
    Optional<User> findByEmailWithRole(@Param("email") String email);

    boolean existsByEmailIgnoreCase(String email);
}