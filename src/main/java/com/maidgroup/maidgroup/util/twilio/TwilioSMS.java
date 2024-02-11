package com.maidgroup.maidgroup.util.twilio;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TwilioSMS {
    private TwilioProperties twilioProperties;

    @Autowired
    public TwilioSMS(TwilioProperties twilioProperties) {
        this.twilioProperties = twilioProperties;
    }

    public void sendSMS(String to, String messageBody) {
        Twilio.init(twilioProperties.getAccountsId() , twilioProperties.getAuthenticationToken());
        Message message = Message.creator(new PhoneNumber(to), new PhoneNumber(twilioProperties.getFromNumber()), messageBody).create();
        System.out.println("SMS sent: " + message.getSid());
    }
}
