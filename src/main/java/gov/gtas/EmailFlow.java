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

    private final
    AttachmentFilter attachmentFilter;

    private final Logger logger = LoggerFactory.getLogger(EmailFlow.class);

    @Autowired
    public EmailFlow(ImapIdleChannelAdapter imapIdleChannelAdapter,
                     JmsTemplate jmsTemplateFile,
                     AttachmentTransformer attachmentTransformer,
                     AttachmentFilter attachmentFilter) {
        this.imapIdleChannelAdapter = imapIdleChannelAdapter;
        this.jmsTemplateFile = jmsTemplateFile;
        this.attachmentTransformer = attachmentTransformer;
        this.attachmentFilter = attachmentFilter;
    }

    @Bean
    IntegrationFlow imapIdleFlow() {
        return IntegrationFlows
                .from(imapIdleChannelAdapter)
                .filter(attachmentFilter)
                .transform(attachmentTransformer)
                .split()
                .handle(this::sendMessage)
                .get();
    }

    private void sendMessage(Message<?> message) {
        jmsTemplateFile.convertAndSend(message);
        logger.info("sent message " + message.getHeaders().get("filename"));
    }

}
