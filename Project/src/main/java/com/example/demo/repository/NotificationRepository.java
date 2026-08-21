package com.example.demo.repository;

import com.example.demo.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** Get all notifications for a user, newest first */
    List<Notification> findByRecipientEmailOrderByCreatedAtDesc(String recipientEmail);

    /** Get only unread notifications for a user */
    List<Notification> findByRecipientEmailAndIsReadFalseOrderByCreatedAtDesc(String recipientEmail);

    /** Count unread notifications for a user */
    long countByRecipientEmailAndIsReadFalse(String recipientEmail);
}
