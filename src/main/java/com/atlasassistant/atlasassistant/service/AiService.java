package com.atlasassistant.atlasassistant.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.atlasassistant.atlasassistant.model.GroqResponse;

@Service
public class AiService {

    @Value("${groq.api.key}")
    private String apiKey;

    public String ask(String context, String question) {
        int maxRetries = 3;
        int attempt = 0;

        while (attempt < maxRetries) {
            try {
                return callGroq(context, question);
            } catch (org.springframework.web.client.HttpServerErrorException e) {
                attempt++;
                if (attempt >= maxRetries) {
                    return "Sorry, the AI service is temporarily busy. Please try again in a moment.";
                }
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException ignored) {}
            }
        }
        return "Sorry, something went wrong.";
    }

    private String callGroq(String context, String question) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        String prompt = "Here is the user's data:\n\n" + context + "\n\nBased only on the data above, answer this question concisely: " + question;

        Map<String, Object> requestBody = Map.of(
            "model", "openai/gpt-oss-120b",
            "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        GroqResponse response = restTemplate.postForObject(
            "https://api.groq.com/openai/v1/chat/completions",
            request,
            GroqResponse.class
        );

        return response.getChoices().get(0).getMessage().getContent();
    }
}