package com.atlasassistant.atlasassistant.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.atlasassistant.atlasassistant.model.SentNotification;
import com.atlasassistant.atlasassistant.model.User;

public interface SentNotificationRepository extends JpaRepository<SentNotification, Long> {
    boolean existsByUserAndNotificationTypeAndUniqueIdentifier(User user, String notificationType, String uniqueIdentifier);
}