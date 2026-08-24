package com.atlasassistant.atlasassistant.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import jakarta.annotation.PostConstruct;

@Service
public class WhatsAppService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.whatsapp-number}")
    private String twilioWhatsAppNumber;

    @Value("${twilio.content-sid}")
    private String contentSid;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    public void sendProactiveMessage(String toWhatsAppNumber, String messageText) {
        try {
            Map<String, String> contentVariables = Map.of("1", messageText);
            String contentVariablesJson = new ObjectMapper().writeValueAsString(contentVariables);

            Message.creator(
                    new PhoneNumber("whatsapp:" + toWhatsAppNumber),
                    new PhoneNumber(twilioWhatsAppNumber),
                    ""
            )
            .setContentSid(contentSid)
            .setContentVariables(contentVariablesJson)
            .create();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}