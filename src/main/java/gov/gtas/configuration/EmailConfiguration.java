/*
 * All Application code is Copyright 2016, The Department of Homeland Security (DHS), U.S. Customs and Border Protection (CBP).
 *
 * Please see LICENSE.txt for details.
 */
package gov.gtas.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
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

@Data
@Configuration
@Component
@EnableAutoConfiguration
@PropertySource("classpath:application.yml")
@ConfigurationProperties(prefix = "java-mail-properties")
public class EmailConfiguration {

    @Value("${email.resource}")
    String emailUrl;

    @Value("${email.should.delete.message}")
    Boolean shouldDeleteMessage;

    @Value("${email.maxProcessedMessagesPerPoll}")
    Integer maxProcessedMessagesPerPoll;

    private Properties javaMailProperties;

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
