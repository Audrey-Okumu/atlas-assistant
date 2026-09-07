package com.atlasassistant.atlasassistant.service;

public class CalendarEvent {
    private final String id;
    private final String summary;

    public CalendarEvent(String id, String summary) {
        this.id = id;
        this.summary = summary;
    }

    public String getId() { return id; }
    public String getSummary() { return summary; }
}