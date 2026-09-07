package com.atlasassistant.atlasassistant.service;

import java.time.Instant;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.atlasassistant.atlasassistant.model.GoogleToken;
import com.atlasassistant.atlasassistant.model.SentNotification;
import com.atlasassistant.atlasassistant.model.User;
import com.atlasassistant.atlasassistant.repository.GoogleTokenRepository;
import com.atlasassistant.atlasassistant.repository.SentNotificationRepository;
import com.atlasassistant.atlasassistant.repository.UserRepository;

@Service
public class ReminderService {

    private final UserRepository userRepository;
    private final GoogleTokenRepository googleTokenRepository;
    private final GoogleTokenService googleTokenService;
    private final CalendarService calendarService;
    private final GmailService gmailService;
    private final AiService aiService;
    private final WhatsAppService whatsAppService;
    private final SentNotificationRepository sentNotificationRepository;

    public ReminderService(UserRepository userRepository,
                            GoogleTokenRepository googleTokenRepository,
                            GoogleTokenService googleTokenService,
                            CalendarService calendarService,
                            GmailService gmailService,
                            AiService aiService,
                            WhatsAppService whatsAppService,
                            SentNotificationRepository sentNotificationRepository) {
        this.userRepository = userRepository;
        this.googleTokenRepository = googleTokenRepository;
        this.googleTokenService = googleTokenService;
        this.calendarService = calendarService;
        this.gmailService = gmailService;
        this.aiService = aiService;
        this.whatsAppService = whatsAppService;
        this.sentNotificationRepository = sentNotificationRepository;
    }

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void checkUpcomingMeetingsAndImportantEmails() {
        List<User> allUsers = userRepository.findAll();

        for (User user : allUsers) {
            if (user.getPhoneNumber() == null) continue;

            GoogleToken googleToken = googleTokenRepository.findByUser(user);
            if (googleToken == null) continue;

            try {
                String accessToken = googleTokenService.getValidAccessToken(googleToken);
                checkUpcomingMeeting(user, accessToken);
                checkImportantEmail(user, accessToken);
            } catch (Exception e) {
                System.err.println("Reminder check failed for user " + user.getEmail() + ": " + e.getMessage());
            }
        }
    }

    private void checkUpcomingMeeting(User user, String accessToken) throws Exception {
        List<CalendarEvent> upcomingEvents = calendarService.getUpcomingEvents(accessToken);
        if (upcomingEvents.isEmpty()) return;

        CalendarEvent nextEvent = upcomingEvents.get(0);

        boolean alreadyNotified = sentNotificationRepository
            .existsByUserAndNotificationTypeAndUniqueIdentifier(user, "MEETING_REMINDER", nextEvent.getId());

        if (alreadyNotified) return;

        whatsAppService.sendProactiveMessage(user.getPhoneNumber(), "Reminder — upcoming: " + nextEvent.getSummary());
        recordNotification(user, "MEETING_REMINDER", nextEvent.getId());
    }

    private void checkImportantEmail(User user, String accessToken) throws Exception {
        List<EmailMessage> emails = gmailService.getRecentEmails(accessToken);
        if (emails.isEmpty()) return;

        String subjectList = emails.stream().map(EmailMessage::getSubject).reduce((a, b) -> a + "\n" + b).orElse("");

        String prompt = "Here are recent email subject lines:\n" + subjectList
            + "\n\nIs any of these likely an important or urgent email (e.g. deadlines, interviews, "
            + "urgent requests, security alerts)? If yes, reply with just that exact subject line. "
            + "If none seem important, reply with exactly: NONE";

        String result = aiService.ask("", prompt).trim();
        if (result.equalsIgnoreCase("NONE")) return;

        EmailMessage matched = emails.stream()
            .filter(e -> e.getSubject().equals(result))
            .findFirst()
            .orElse(null);

        if (matched == null) return;

        boolean alreadyNotified = sentNotificationRepository
            .existsByUserAndNotificationTypeAndUniqueIdentifier(user, "IMPORTANT_EMAIL", matched.getId());

        if (alreadyNotified) return;

        whatsAppService.sendProactiveMessage(user.getPhoneNumber(), "Important email: " + matched.getSubject());
        recordNotification(user, "IMPORTANT_EMAIL", matched.getId());
    }

    private void recordNotification(User user, String type, String uniqueId) {
        SentNotification notification = new SentNotification();
        notification.setUser(user);
        notification.setNotificationType(type);
        notification.setUniqueIdentifier(uniqueId);
        notification.setSentAt(Instant.now());
        sentNotificationRepository.save(notification);
    }
}