package com.rws.email.emailtoqueue.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.integration.mail.*;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Configuration
@Component
@PropertySource("classpath:application.yml")
public class EmailConfiguration {

    @Value("${email.resource}")
    String emailUrl;

    @Value("${email.should.delete.message}")
    Boolean shouldDeleteMessage;

    private final
    Properties javaMailProperties;

    @Autowired
    public EmailConfiguration(@Qualifier("javaProperties") Properties javaMailProperties) {
        this.javaMailProperties = javaMailProperties;
    }

    @Bean
    @Qualifier("inboundMessageAdapter")
    ImapIdleChannelAdapter imapIdleChannelAdapter() {
        return new ImapIdleChannelAdapter(imapMailReceiver());
    }

    @Bean
    ImapMailReceiver imapMailReceiver() {
        ImapMailReceiver imapMailReceiver = new ImapMailReceiver(emailUrl);
        imapMailReceiver.setShouldDeleteMessages(shouldDeleteMessage);
        imapMailReceiver.setJavaMailProperties(javaMailProperties);
        return imapMailReceiver;
    }
}
