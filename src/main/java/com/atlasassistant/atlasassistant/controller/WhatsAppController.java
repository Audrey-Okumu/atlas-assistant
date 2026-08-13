package com.atlasassistant.atlasassistant.controller;

import com.atlasassistant.atlasassistant.model.GoogleToken;
import com.atlasassistant.atlasassistant.model.User;
import com.atlasassistant.atlasassistant.repository.GoogleTokenRepository;
import com.atlasassistant.atlasassistant.repository.UserRepository;
import com.atlasassistant.atlasassistant.service.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/webhook/whatsapp")
public class WhatsAppController {

    @Value("${whatsapp.verify-token}")
    private String verifyToken;

    private final UserRepository userRepository;
    private final GoogleTokenRepository googleTokenRepository;
    private final GoogleTokenService googleTokenService;
    private final GmailService gmailService;
    private final CalendarService calendarService;
    private final AiService aiService;
    private final WhatsAppService whatsAppService;

    public WhatsAppController(UserRepository userRepository, GoogleTokenRepository googleTokenRepository,
                               GoogleTokenService googleTokenService, GmailService gmailService,
                               CalendarService calendarService, AiService aiService,
                               WhatsAppService whatsAppService) {
        this.userRepository = userRepository;
        this.googleTokenRepository = googleTokenRepository;
        this.googleTokenService = googleTokenService;
        this.gmailService = gmailService;
        this.calendarService = calendarService;
        this.aiService = aiService;
        this.whatsAppService = whatsAppService;
    }

    @GetMapping
    public String verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {

        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            return challenge;
        }
        throw new RuntimeException("Webhook verification failed");
    }

    @PostMapping
    @SuppressWarnings("unchecked")
    public String receiveMessage(@RequestBody Map<String, Object> payload) throws Exception {
        try {
            List<Map<String, Object>> entry = (List<Map<String, Object>>) payload.get("entry");
            Map<String, Object> changes = (Map<String, Object>) ((List<Map<String, Object>>) entry.get(0).get("changes")).get(0);
            Map<String, Object> value = (Map<String, Object>) changes.get("value");
            List<Map<String, Object>> messages = (List<Map<String, Object>>) value.get("messages");

            if (messages == null || messages.isEmpty()) {
                return "no message";
            }

            Map<String, Object> message = messages.get(0);
            String fromPhoneNumber = (String) message.get("from");
            Map<String, Object> text = (Map<String, Object>) message.get("text");
            String messageBody = (String) text.get("body");

            String reply = handleUserQuestion(fromPhoneNumber, messageBody);

            whatsAppService.sendMessage(fromPhoneNumber, reply);

            return "ok";
        } catch (Exception e) {
            return "ignored: " + e.getMessage();
        }
    }

    private String handleUserQuestion(String fromPhoneNumber, String question) throws Exception {
        // TODO: once real WhatsApp numbers are linked to Atlas Assistant users
        // (a mapping we haven't built yet), look up the correct User by phone number
        // instead of hardcoding. Tracked as real, known technical debt.
        User user = userRepository.findByEmail("akelloaudrey3@gmail.com");
        GoogleToken googleToken = googleTokenRepository.findByUser(user);
        String accessToken = googleTokenService.getValidAccessToken(googleToken);

        List<String> emails = gmailService.getRecentEmailSubjects(accessToken);
        List<String> events = calendarService.getUpcomingEvents(accessToken);

        String currentDateTime = java.time.ZonedDateTime.now().toString();
        String context = "Current date and time: " + currentDateTime
            + "\n\nRecent emails:\n" + String.join("\n", emails)
            + "\n\nUpcoming calendar events:\n" + String.join("\n", events);

        return aiService.ask(context, question);
    }
}