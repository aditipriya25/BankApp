package com.example.demo.service;

import com.example.demo.model.Notification;
import com.example.demo.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * NotificationService — Creates and manages in-app notifications.
 *
 * Called by: KycService, LockerClosureService, RentPaymentService, LockerAgreementService.
 *
 * Types:
 *   KYC_SUBMITTED, KYC_APPROVED, KYC_REJECTED
 *   RENT_PAID, RENT_DUE
 *   CLOSURE_REQUESTED, CLOSURE_APPROVED, CLOSURE_REJECTED
 *   AGREEMENT_READY, AGREEMENT_SIGNED
 *   GENERAL
 */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // ── Create ────────────────────────────────────────────────────────────────

    /**
     * Create and persist a notification.
     *
     * @param recipientEmail  Target user's email
     * @param recipientRole   "CUSTOMER" or "EMPLOYEE"
     * @param title           Short title (shown in bell dropdown)
     * @param message         Full message body
     * @param type            Event type (e.g. KYC_APPROVED, RENT_DUE)
     */
    public Notification createNotification(String recipientEmail, String recipientRole,
                                           String title, String message, String type) {
        Notification n = new Notification();
        n.setRecipientEmail(recipientEmail);
        n.setRecipientRole(recipientRole);
        n.setTitle(title);
        n.setMessage(message);
        n.setType(type);
        n.setRead(false);
        n.setCreatedAt(LocalDateTime.now());
        return notificationRepository.save(n);
    }

    // ── Query ─────────────────────────────────────────────────────────────────

    /** All notifications for a user (newest first) */
    public List<Notification> getAllForUser(String email) {
        return notificationRepository.findByRecipientEmailOrderByCreatedAtDesc(email);
    }

    /** Unread notifications for a user */
    public List<Notification> getUnreadForUser(String email) {
        return notificationRepository.findByRecipientEmailAndIsReadFalseOrderByCreatedAtDesc(email);
    }

    /** Unread count for a user */
    public long getUnreadCount(String email) {
        return notificationRepository.countByRecipientEmailAndIsReadFalse(email);
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    /** Mark a single notification as read */
    @Transactional
    public void markRead(Long notificationId, String requesterEmail) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        if (!n.getRecipientEmail().equals(requesterEmail)) {
            throw new RuntimeException("Access denied");
        }
        n.setRead(true);
        notificationRepository.save(n);
    }

    /** Mark all notifications as read for a user */
    @Transactional
    public void markAllRead(String email) {
        List<Notification> unread = notificationRepository
                .findByRecipientEmailAndIsReadFalseOrderByCreatedAtDesc(email);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }
}
