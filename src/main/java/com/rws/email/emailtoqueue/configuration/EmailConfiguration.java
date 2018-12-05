package com.rws.email.emailtoqueue.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.integration.mail.ImapIdleChannelAdapter;
import org.springframework.integration.mail.ImapMailReceiver;
import org.springframework.integration.mail.SearchTermStrategy;
import org.springframework.stereotype.Component;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.search.FlagTerm;
import javax.mail.search.SearchTerm;
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

    @Autowired
    public EmailConfiguration(@Qualifier("javaProperties") Properties javaMailProperties) {
        this.javaMailProperties = javaMailProperties;
    }

    @Bean
    @Qualifier("imapIdleAdapter")
    ImapIdleChannelAdapter imapIdleAdapter() {
        return new ImapIdleChannelAdapter(imapMailReceiver());
    }

    @Bean
    ImapMailReceiver imapMailReceiver() {
        ImapMailReceiver imapMailReceiver = new ImapMailReceiver(emailUrl);
        imapMailReceiver.setShouldDeleteMessages(shouldDeleteMessage);
        imapMailReceiver.setJavaMailProperties(javaMailProperties);
        imapMailReceiver.setMaxFetchSize(maxProcessedMessagesPerPoll);
        imapMailReceiver.setSearchTermStrategy(searchTermsForImap());
        return imapMailReceiver;
    }

    private SearchTermStrategy searchTermsForImap() {
        return new ReceiverSearchTerms();
    }

    public class ReceiverSearchTerms implements SearchTermStrategy {
        @Override
        public SearchTerm generateSearchTerm(Flags flags, Folder folder) {
            return new FlagTerm(new Flags(Flags.Flag.SEEN), false);
        }
    }
}
