package com.academy.tms.repository;

import com.academy.tms.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("""
           select n from Notification n
           where lower(n.user.email) = lower(:email)
           order by n.createdAt desc
           """)
    List<Notification> findRecentByEmail(@Param("email") String email);

    @Query("select count(n) from Notification n where lower(n.user.email) = lower(:email) and n.read = false")
    long countUnreadByEmail(@Param("email") String email);

    @Modifying
    @Query("update Notification n set n.read = true where lower(n.user.email) = lower(:email) and n.read = false")
    int markAllReadByEmail(@Param("email") String email);

    @Modifying
    @Query("delete from Notification n where n.user.id = :userId")
    int deleteAllByUserId(@Param("userId") Long userId);
}
