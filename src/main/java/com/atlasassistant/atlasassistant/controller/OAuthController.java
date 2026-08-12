package com.atlasassistant.atlasassistant.controller;

import java.security.Principal;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OAuthController {

    private final OAuth2AuthorizedClientService authorizedClientService;

    public OAuthController(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    @GetMapping("/oauth2/success")
    public String oauth2Success(Principal principal) {
        OAuth2AuthorizedClient client =
            authorizedClientService.loadAuthorizedClient("google", principal.getName());

        OAuth2AccessToken accessToken = client.getAccessToken();
        OAuth2RefreshToken refreshToken = client.getRefreshToken();

        String accessTokenValue = accessToken.getTokenValue();
        String refreshTokenValue = (refreshToken != null) ? refreshToken.getTokenValue() : "NO REFRESH TOKEN RECEIVED";

        return "Access token (first 20 chars): " + accessTokenValue.substring(0, 20)
            + "... | Refresh token present: " + (refreshToken != null);
    }
}