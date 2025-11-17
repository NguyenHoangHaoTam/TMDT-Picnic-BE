package com.java.TMDTPicnic.repository;

import com.java.TMDTPicnic.entity.Notification;
import com.java.TMDTPicnic.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    List<Notification> findByUserAndReadFlagOrderByCreatedAtDesc(User user, Boolean readFlag);

    Long countByUserAndReadFlag(User user, Boolean readFlag);

    @Modifying
    @Query("UPDATE Notification n SET n.readFlag = true WHERE n.user = :user")
    void markAllAsReadByUser(@Param("user") User user);
}

