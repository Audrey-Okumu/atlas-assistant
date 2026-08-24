package com.atlasassistant.atlasassistant.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atlasassistant.atlasassistant.model.GoogleToken;
import com.atlasassistant.atlasassistant.model.User;
import com.atlasassistant.atlasassistant.repository.GoogleTokenRepository;
import com.atlasassistant.atlasassistant.repository.UserRepository;
import com.atlasassistant.atlasassistant.service.AiService;
import com.atlasassistant.atlasassistant.service.CalendarService;
import com.atlasassistant.atlasassistant.service.GmailService;
import com.atlasassistant.atlasassistant.service.GoogleTokenService;

@RestController
@RequestMapping("/webhook/whatsapp")
public class WhatsAppController {

    private final UserRepository userRepository;
    private final GoogleTokenRepository googleTokenRepository;
    private final GoogleTokenService googleTokenService;
    private final GmailService gmailService;
    private final CalendarService calendarService;
    private final AiService aiService;

    public WhatsAppController(UserRepository userRepository, GoogleTokenRepository googleTokenRepository,
                               GoogleTokenService googleTokenService, GmailService gmailService,
                               CalendarService calendarService, AiService aiService) {
        this.userRepository = userRepository;
        this.googleTokenRepository = googleTokenRepository;
        this.googleTokenService = googleTokenService;
        this.gmailService = gmailService;
        this.calendarService = calendarService;
        this.aiService = aiService;
    }

    @PostMapping(produces = MediaType.APPLICATION_XML_VALUE)
    public String receiveMessage(@RequestParam("Body") String body) {
        String reply;
        try {
            reply = handleUserQuestion(body);
        } catch (Exception e) {
            e.printStackTrace();
            reply = "Sorry, something went wrong processing your request.";
        }

        String safeReply = reply.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<Response><Message>" + safeReply + "</Message></Response>";
    }

    private String handleUserQuestion(String question) throws Exception {
        // TODO: once real WhatsApp numbers are linked to Atlas Assistant users,
        // look up the correct User by phone number instead of hardcoding.
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