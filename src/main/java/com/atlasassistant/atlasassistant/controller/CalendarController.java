package com.atlasassistant.atlasassistant.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atlasassistant.atlasassistant.model.GoogleToken;
import com.atlasassistant.atlasassistant.model.User;
import com.atlasassistant.atlasassistant.repository.GoogleTokenRepository;
import com.atlasassistant.atlasassistant.repository.UserRepository;
import com.atlasassistant.atlasassistant.service.CalendarService;
import com.atlasassistant.atlasassistant.service.GoogleTokenService;

@RestController
@RequestMapping("/calendar")
public class CalendarController {

    private final UserRepository userRepository;
    private final GoogleTokenRepository googleTokenRepository;
    private final CalendarService calendarService;
    private final GoogleTokenService googleTokenService;

    public CalendarController(UserRepository userRepository, GoogleTokenRepository googleTokenRepository,
                               CalendarService calendarService, GoogleTokenService googleTokenService) {
        this.userRepository = userRepository;
        this.googleTokenRepository = googleTokenRepository;
        this.calendarService = calendarService;
        this.googleTokenService = googleTokenService;
    }

    @GetMapping("/upcoming")
    public List<String> getUpcomingEvents(Principal principal) throws Exception {
        User user = userRepository.findByEmail(principal.getName());
        GoogleToken googleToken = googleTokenRepository.findByUser(user);

        String validAccessToken = googleTokenService.getValidAccessToken(googleToken);

        return calendarService.getUpcomingEvents(validAccessToken);
    }
}