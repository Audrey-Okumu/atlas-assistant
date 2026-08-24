package com.atlasassistant.atlasassistant.service;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.atlasassistant.atlasassistant.model.GoogleToken;
import com.atlasassistant.atlasassistant.model.User;
import com.atlasassistant.atlasassistant.repository.GoogleTokenRepository;
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

    public ReminderService(UserRepository userRepository,
                            GoogleTokenRepository googleTokenRepository,
                            GoogleTokenService googleTokenService,
                            CalendarService calendarService,
                            GmailService gmailService,
                            AiService aiService,
                            WhatsAppService whatsAppService) {
        this.userRepository = userRepository;
        this.googleTokenRepository = googleTokenRepository;
        this.googleTokenService = googleTokenService;
        this.calendarService = calendarService;
        this.gmailService = gmailService;
        this.aiService = aiService;
        this.whatsAppService = whatsAppService;
    }

    @Scheduled(fixedRate = 5 * 60 * 1000) // every 5 minutes
    public void checkUpcomingMeetingsAndImportantEmails() {
        List<User> allUsers = userRepository.findAll();

        for (User user : allUsers) {
            if (user.getPhoneNumber() == null) {
                continue;
            }

            GoogleToken googleToken = googleTokenRepository.findByUser(user);
            if (googleToken == null) {
                continue;
            }

            try {
                String accessToken = googleTokenService.getValidAccessToken(googleToken);

                checkUpcomingMeeting(user, accessToken);
                checkImportantEmail(user, accessToken);

            } catch (Exception e) {
                System.err.println("Reminder check failed for user " + user.getEmail() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void checkUpcomingMeeting(User user, String accessToken) throws Exception {
        List<String> upcomingEvents = calendarService.getUpcomingEvents(accessToken);

        if (!upcomingEvents.isEmpty()) {
            String reminder = "Reminder — upcoming: " + upcomingEvents.get(0);
            whatsAppService.sendProactiveMessage(user.getPhoneNumber(), reminder);
        }
    }

    private void checkImportantEmail(User user, String accessToken) throws Exception {
        List<String> emailSubjects = gmailService.getRecentEmailSubjects(accessToken);

        if (emailSubjects.isEmpty()) {
            return;
        }

        String prompt = "Here are recent email subject lines:\n" + String.join("\n", emailSubjects)
            + "\n\nIs any of these likely an important or urgent email (e.g. deadlines, interviews, "
            + "urgent requests, security alerts)? If yes, reply with just that exact subject line. "
            + "If none seem important, reply with exactly: NONE";

        String result = aiService.ask("", prompt).trim();

        if (!result.equalsIgnoreCase("NONE")) {
            whatsAppService.sendProactiveMessage(user.getPhoneNumber(), "Important email: " + result);
        }
    }
}