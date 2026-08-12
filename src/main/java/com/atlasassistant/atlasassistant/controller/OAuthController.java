package com.atlasassistant.atlasassistant.controller;

import java.security.Principal;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atlasassistant.atlasassistant.config.JwtUtil;
import com.atlasassistant.atlasassistant.model.GoogleToken;
import com.atlasassistant.atlasassistant.model.User;
import com.atlasassistant.atlasassistant.repository.GoogleTokenRepository;
import com.atlasassistant.atlasassistant.repository.UserRepository;


@RestController
public class OAuthController {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final UserRepository userRepository;
    private final GoogleTokenRepository googleTokenRepository;
    private final JwtUtil jwtUtil;

    public OAuthController(OAuth2AuthorizedClientService authorizedClientService,
                            UserRepository userRepository,
                            GoogleTokenRepository googleTokenRepository,
                            JwtUtil jwtUtil) {
        this.authorizedClientService = authorizedClientService;
        this.userRepository = userRepository;
        this.googleTokenRepository = googleTokenRepository;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/oauth2/success")
        public String oauth2Success(Principal principal, @AuthenticationPrincipal OAuth2User oauth2User) {
        OAuth2AuthorizedClient client =
            authorizedClientService.loadAuthorizedClient("google", principal.getName());

        OAuth2AccessToken accessToken = client.getAccessToken();
        OAuth2RefreshToken refreshToken = client.getRefreshToken();

        String email = oauth2User.getAttribute("email");

        User user = userRepository.findByEmail(email);
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setName(oauth2User.getAttribute("name"));
            user = userRepository.save(user);
        }

        GoogleToken googleToken = googleTokenRepository.findByUser(user);
        if (googleToken == null) {
            googleToken = new GoogleToken();
            googleToken.setUser(user);
        }

        googleToken.setAccessToken(accessToken.getTokenValue());
        if (refreshToken != null) {
            googleToken.setRefreshToken(refreshToken.getTokenValue());
        }
        googleToken.setAccessTokenExpiresAt(accessToken.getExpiresAt());

        googleTokenRepository.save(googleToken);
        String appJwt = jwtUtil.generateToken(user.getEmail());

        return "Google account connected successfully for " + email + " | Your Atlas Assistant token: " + appJwt;
    }
}