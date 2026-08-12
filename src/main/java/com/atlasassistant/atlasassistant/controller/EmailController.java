package com.atlasassistant.atlasassistant.controller;

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

    public EmailController(UserRepository userRepository, GoogleTokenRepository googleTokenRepository, GmailService gmailService, GoogleTokenService googleTokenService) {
        this.userRepository = userRepository;
        this.googleTokenRepository = googleTokenRepository;
        this.gmailService = gmailService;
        this.googleTokenService = googleTokenService;
    }


    @GetMapping("/recent")
    public List<String> getRecentEmails() throws Exception {
        User user = userRepository.findByEmail("akelloaudrey3@gmail.com");
        GoogleToken googleToken = googleTokenRepository.findByUser(user);

        String validAccessToken = googleTokenService.getValidAccessToken(googleToken);

        return gmailService.getRecentEmailSubjects(validAccessToken);
    }
}