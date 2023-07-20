package com.maidgroup.maidgroup.util.sendgrid;

import com.maidgroup.maidgroup.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Value("${sendgrid.api-key}")
    private String sendGridApiKey;

    @Bean
    public EmailService emailService() {
        return new EmailService(sendGridApiKey);
    }

}
