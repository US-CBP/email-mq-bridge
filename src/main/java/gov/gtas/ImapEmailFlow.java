/*
 * All Application code is Copyright 2016, The Department of Homeland Security (DHS), U.S. Customs and Border Protection (CBP).
 *
 * Please see LICENSE.txt for details.
 */
package gov.gtas;

import gov.gtas.flowobjects.AttachmentFilter;
import gov.gtas.flowobjects.AttachmentTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.mail.MailReceivingMessageSource;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


@Component
@ConditionalOnProperty(value = "email.protocol", havingValue = "imap")
@PropertySource("classpath:application.yml")
public class ImapEmailFlow {

    private final
    JmsTemplate jmsTemplateFile;

    private final
    MailReceivingMessageSource mailReceivingMessageSource;

    private final
    AttachmentTransformer attachmentTransformer;

    private final
    AttachmentFilter attachmentFilter;

    private final Logger logger = LoggerFactory.getLogger(ImapEmailFlow.class);

    @Value("${email.maxProcessedMessagesPerPoll}")
    Integer maxThreadsPerPoll;

    @Value("${email.pollTimeInSeconds}")
    Integer pollTimeInSeconds;

    @Value("${mq.on}")
    Boolean activeMqOn;

    @Autowired
    public ImapEmailFlow(@Qualifier("imapAdapter") MailReceivingMessageSource mailReceivingMessageSource,
                         JmsTemplate jmsTemplateFile,
                         AttachmentTransformer attachmentTransformer,
                         AttachmentFilter attachmentFilter) {
        this.mailReceivingMessageSource = mailReceivingMessageSource;
        this.jmsTemplateFile = jmsTemplateFile;
        this.attachmentTransformer = attachmentTransformer;
        this.attachmentFilter = attachmentFilter;
    }

    @Bean
    @ConditionalOnProperty(value = "email.protocol", havingValue = "imap")
    IntegrationFlow imapPollingFlow() {
        return IntegrationFlows
                .from(mailReceivingMessageSource, inboundMailConfig -> inboundMailConfig
                        .poller(
                                Pollers.fixedDelay(pollTimeInSeconds, TimeUnit.SECONDS)
                                        .maxMessagesPerPoll(maxThreadsPerPoll)
                        )
                )
                .filter(attachmentFilter)
                .transform(attachmentTransformer)
                .split()
                .handle(this::sendMessage)
                .get();
    }

    @SuppressWarnings("Duplicates")
    private void sendMessage(Message<?> message) {
        if (activeMqOn) {
            jmsTemplateFile.send(session -> {
                javax.jms.Message m = session.createObjectMessage((String)message.getPayload());
                m.setStringProperty("filename", message.getHeaders().get("filename").toString());
                return m;
            });
            logger.info("Message put on queue" + message.getHeaders().get("filename"));
        } else {
            logger.info("Message processed - MQ is OFF " + message.getHeaders().get("filename"));
        }
    }
}
