package com.atlasassistant.atlasassistant.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.whatsapp-number}")
    private String twilioWhatsAppNumber;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    public void sendMessage(String toWhatsAppNumber, String messageText) {
        Message.creator(
                new PhoneNumber("whatsapp:" + toWhatsAppNumber),
                new PhoneNumber(twilioWhatsAppNumber),
                messageText
        ).create();
    }
}