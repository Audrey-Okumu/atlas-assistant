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
import com.atlasassistant.atlasassistant.service.GmailService;
import com.atlasassistant.atlasassistant.service.GoogleTokenService;

@RestController
@RequestMapping("/emails")
public class EmailController {

    private final UserRepository userRepository;
    private final GoogleTokenRepository googleTokenRepository;
    private final GmailService gmailService;
    private final GoogleTokenService googleTokenService;

    public EmailController(UserRepository userRepository, GoogleTokenRepository googleTokenRepository,
                            GmailService gmailService, GoogleTokenService googleTokenService) {
        this.userRepository = userRepository;
        this.googleTokenRepository = googleTokenRepository;
        this.gmailService = gmailService;
        this.googleTokenService = googleTokenService;
    }

    @GetMapping("/recent")
    public List<String> getRecentEmails(Principal principal) throws Exception {
        String email = principal.getName();

        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("Authenticated user not found in database: " + email);
        }

        GoogleToken googleToken = googleTokenRepository.findByUser(user);
        if (googleToken == null) {
            throw new RuntimeException("No Google account connected for user: " + email);
        }

        String validAccessToken = googleTokenService.getValidAccessToken(googleToken);

        return gmailService.getRecentEmailSubjects(validAccessToken);
    }
}