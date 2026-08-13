package com.atlasassistant.atlasassistant.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/assistant")
public class AssistantController {

    private final UserRepository userRepository;
    private final GoogleTokenRepository googleTokenRepository;
    private final GoogleTokenService googleTokenService;
    private final GmailService gmailService;
    private final CalendarService calendarService;
    private final AiService aiService;

    public AssistantController(UserRepository userRepository, GoogleTokenRepository googleTokenRepository,
                                GoogleTokenService googleTokenService, GmailService gmailService,
                                CalendarService calendarService, AiService aiService) {
        this.userRepository = userRepository;
        this.googleTokenRepository = googleTokenRepository;
        this.googleTokenService = googleTokenService;
        this.gmailService = gmailService;
        this.calendarService = calendarService;
        this.aiService = aiService;
    }

    @GetMapping("/ask")
    public String ask(Principal principal, @RequestParam String question) throws Exception {
        User user = userRepository.findByEmail(principal.getName());
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