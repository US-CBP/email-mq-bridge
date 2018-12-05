package com.rws.email.emailtoqueue;

import com.rws.email.emailtoqueue.flowobjects.AttachmentSplitter;
import com.rws.email.emailtoqueue.flowobjects.AttachmentTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.mail.ImapIdleChannelAdapter;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;


@Component
@PropertySource("classpath:application.yml")
public class EmailFlow {

    private final
    ImapIdleChannelAdapter imapIdleChannelAdapter;
    private final
    JmsTemplate jmsTemplateFile;
    private final
    AttachmentTransformer attachmentTransformer;
    private final AttachmentSplitter attachmentSplitter;
    private Logger logger = LoggerFactory.getLogger(EmailFlow.class);

    @Autowired
    public EmailFlow(ImapIdleChannelAdapter imapIdleChannelAdapter,
                     JmsTemplate jmsTemplateFile,
                     AttachmentTransformer attachmentTransformer,
                     AttachmentSplitter attachmentSplitter) {
        this.imapIdleChannelAdapter = imapIdleChannelAdapter;
        this.jmsTemplateFile = jmsTemplateFile;
        this.attachmentTransformer = attachmentTransformer;
        this.attachmentSplitter = attachmentSplitter;
    }

/*

    private final
    ImapIdleChannelAdapter imapIdleChannelAdapter;

    private final
    JmsTemplate jmsTemplateFile;

    @Value("${email.maxProcessedMessagesPerPoll}")
    Integer maxThreadsPerPoll;
    @Value("${email.pollTimeInSeconds}")
    Integer pollTimeInSeconds;


    IntegrationFlow pollingFlow() {
        return IntegrationFlows
                .from(mailReceivingMessageSource, inboundMailConfig -> inboundMailConfig
                        .poller(
                                Pollers.fixedRate(pollTimeInSeconds, TimeUnit.SECONDS)
                                        .maxMessagesPerPoll(maxThreadsPerPoll)
                        )
                )
                .transform(attachmentTransformer())
                .split(attachmentSplitter())
                .handle(this::sendMessage)
                .get();
    }
*/

    @Bean
    IntegrationFlow imapIdleFlow() {
        return IntegrationFlows
                .from(imapIdleChannelAdapter)
                .transform(attachmentTransformer)
                .split(attachmentSplitter)
                .handle(this::sendMessage)
                .get();
    }

    private void sendMessage(Message<?> message) {
        jmsTemplateFile.convertAndSend(message);
        logger.info("successfully sent message!");
    }

}
