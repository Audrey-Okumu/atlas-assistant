package com.atlasassistant.atlasassistant.service;

import com.atlasassistant.atlasassistant.model.GoogleToken;
import com.atlasassistant.atlasassistant.repository.GoogleTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;

@Service
public class GoogleTokenService {

    private final GoogleTokenRepository googleTokenRepository;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    public GoogleTokenService(GoogleTokenRepository googleTokenRepository) {
        this.googleTokenRepository = googleTokenRepository;
    }

    public String getValidAccessToken(GoogleToken googleToken) {
        boolean expired = googleToken.getAccessTokenExpiresAt() == null
            || googleToken.getAccessTokenExpiresAt().isBefore(Instant.now().plusSeconds(60));

        if (!expired) {
            return googleToken.getAccessToken();
        }

        return refreshAccessToken(googleToken);
    }

    private String refreshAccessToken(GoogleToken googleToken) {
    RestTemplate restTemplate = new RestTemplate();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    org.springframework.util.MultiValueMap<String, String> body = new org.springframework.util.LinkedMultiValueMap<>();
    body.add("client_id", clientId);
    body.add("client_secret", clientSecret);
    body.add("refresh_token", googleToken.getRefreshToken());
    body.add("grant_type", "refresh_token");

    HttpEntity<org.springframework.util.MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

    ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
        "https://oauth2.googleapis.com/token",
        HttpMethod.POST,
        request,
        new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
    );

    Map<String, Object> responseBody = response.getBody();

    String newAccessToken = (String) responseBody.get("access_token");
    Integer expiresIn = (Integer) responseBody.get("expires_in");

    googleToken.setAccessToken(newAccessToken);
    googleToken.setAccessTokenExpiresAt(Instant.now().plusSeconds(expiresIn));
    googleTokenRepository.save(googleToken);

    return newAccessToken;
}
}