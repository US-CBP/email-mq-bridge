package gov.gtas;

import gov.gtas.flowobjects.AttachmentFilter;
import gov.gtas.flowobjects.AttachmentTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@ConditionalOnProperty(value = "email.protocol", havingValue = "pop3")
@PropertySource("classpath:application.yml")
public class Pop3Flow {
    private final
    JmsTemplate jmsTemplateFile;

    private final
    MailReceivingMessageSource mailReceivingMessageSource;

    private final
    AttachmentTransformer attachmentTransformer;

    private final
    AttachmentFilter attachmentFilter;

    private final Logger logger = LoggerFactory.getLogger(Pop3Flow.class);

    @Value("${email.maxProcessedMessagesPerPoll}")
    Integer maxThreadsPerPoll;

    @Value("${email.pollTimeInSeconds}")
    Integer pollTimeInSeconds;

    @Value("${mq.on}")
    Boolean activeMqOn;

    public Pop3Flow(JmsTemplate jmsTemplateFile, @Qualifier("pop3Adapter") MailReceivingMessageSource mailReceivingMessageSource, AttachmentTransformer attachmentTransformer, AttachmentFilter attachmentFilter) {
        this.jmsTemplateFile = jmsTemplateFile;
        this.mailReceivingMessageSource = mailReceivingMessageSource;
        this.attachmentTransformer = attachmentTransformer;
        this.attachmentFilter = attachmentFilter;
    }

    @Bean
    @ConditionalOnProperty(value = "email.protocol", havingValue = "pop3")
    IntegrationFlow pop3PollingFlow() {
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
