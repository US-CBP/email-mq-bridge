package com.rws.email.emailtoqueue.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.mail.ImapMailReceiver;
import org.springframework.integration.mail.MailReceiver;
import org.springframework.integration.mail.MailReceivingMessageSource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
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

    @Value("${email.maxProcessedMessagesPerPoll}")
    Integer maxProcessedMessagesPerPoll;

    private final
    Properties javaMailProperties;

    private final
    ReceiverSearchTerms receiverSearchTerms;

    @Autowired
    public EmailConfiguration(@Qualifier("javaProperties") Properties javaMailProperties, ReceiverSearchTerms receiverSearchTerms) {
        this.javaMailProperties = javaMailProperties;
        this.receiverSearchTerms = receiverSearchTerms;
    }

    @Bean
    @Qualifier("inboundMessageAdapter")
    MailReceivingMessageSource mailReceivingMessageSource() {
        return new MailReceivingMessageSource(imapMailReceiver());
    }

    @Bean
    MailReceiver imapMailReceiver() {
        ImapMailReceiver imapMailReceiver = new ImapMailReceiver(emailUrl);
        imapMailReceiver.setShouldDeleteMessages(shouldDeleteMessage);
        imapMailReceiver.setJavaMailProperties(javaMailProperties);
        imapMailReceiver.setSearchTermStrategy(receiverSearchTerms);
        return imapMailReceiver;
    }

    @Bean
    @Qualifier("emailTaskExecutor")
    TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(maxProcessedMessagesPerPoll);
        return taskExecutor;
    }

}
