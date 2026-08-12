package com.atlasassistant.atlasassistant.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

@Service
public class GmailService {

    public List<String> getRecentEmailSubjects(String accessToken) throws Exception {
        Credential credential = new Credential(com.google.api.client.auth.oauth2.BearerToken.authorizationHeaderAccessMethod())
            .setAccessToken(accessToken);

        Gmail gmailService = new Gmail.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                credential)
            .setApplicationName("Atlas Assistant")
            .build();

        ListMessagesResponse response = gmailService.users().messages()
            .list("me")
            .setMaxResults(5L)
            .execute();

        List<String> subjects = new ArrayList<>();

        if (response.getMessages() != null) {
            for (Message messageMeta : response.getMessages()) {
                Message fullMessage = gmailService.users().messages()
                    .get("me", messageMeta.getId())
                    .setFormat("metadata")
                    .setMetadataHeaders(List.of("Subject"))
                    .execute();

                String subject = fullMessage.getPayload().getHeaders().stream()
                    .filter(h -> h.getName().equals("Subject"))
                    .findFirst()
                    .map(h -> h.getValue())
                    .orElse("(No subject)");

                subjects.add(subject);
            }
        }

        return subjects;
    }
}