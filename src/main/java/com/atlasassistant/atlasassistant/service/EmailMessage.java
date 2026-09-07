package com.atlasassistant.atlasassistant.service;

public class EmailMessage {
    private final String id;
    private final String subject;

    public EmailMessage(String id, String subject) {
        this.id = id;
        this.subject = subject;
    }

    public String getId() { return id; }
    public String getSubject() { return subject; }
}