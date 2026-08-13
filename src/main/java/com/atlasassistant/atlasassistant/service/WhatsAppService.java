package com.atlasassistant.atlasassistant.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WhatsAppService {

    @Value("${whatsapp.access-token}")
    private String accessToken;

    @Value("${whatsapp.phone-number-id}")
    private String phoneNumberId;

    public void sendMessage(String toPhoneNumber, String messageText) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        Map<String, Object> requestBody = Map.of(
            "messaging_product", "whatsapp",
            "to", toPhoneNumber,
            "type", "text",
            "text", Map.of("body", messageText)
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        String url = "https://graph.facebook.com/v21.0/" + phoneNumberId + "/messages";

        restTemplate.postForEntity(url, request, String.class);
    }
}