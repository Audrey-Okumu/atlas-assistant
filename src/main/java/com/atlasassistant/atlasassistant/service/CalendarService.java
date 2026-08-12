package com.atlasassistant.atlasassistant.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

@Service
public class CalendarService {

    public List<String> getUpcomingEvents(String accessToken) throws Exception {
        Credential credential = new Credential(com.google.api.client.auth.oauth2.BearerToken.authorizationHeaderAccessMethod())
            .setAccessToken(accessToken);

        Calendar calendarService = new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                credential)
            .setApplicationName("Atlas Assistant")
            .build();

        com.google.api.client.util.DateTime now = new com.google.api.client.util.DateTime(System.currentTimeMillis());

        Events events = calendarService.events().list("primary")
            .setMaxResults(5)
            .setTimeMin(now)
            .setOrderBy("startTime")
            .setSingleEvents(true)
            .execute();

        List<String> summaries = new ArrayList<>();

        for (Event event : events.getItems()) {
            String start = (event.getStart().getDateTime() != null)
                ? event.getStart().getDateTime().toString()
                : event.getStart().getDate().toString();

            summaries.add(event.getSummary() + " at " + start);
        }

        return summaries;
    }
}