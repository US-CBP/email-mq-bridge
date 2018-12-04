package com.rws.email.emailtoqueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OutboundQueue {

    private Logger logger = LoggerFactory.getLogger(OutboundQueue.class);

    private final
    JmsTemplate jmsTemplateFile;

    @Autowired
    public OutboundQueue(JmsTemplate jmsTemplateFile) {
        this.jmsTemplateFile = jmsTemplateFile;
    }

    void putAttachmentsOnQueue(Map<String, String> attachmentNameAsKeyAttachmentAsValue) {
        for (String attachmentName : attachmentNameAsKeyAttachmentAsValue.keySet()) {
            String payload = attachmentNameAsKeyAttachmentAsValue.get(attachmentName);
            Message message = MessageBuilder
                    .withPayload(payload)
                    .setHeader("filename", attachmentName)
                    .build();
            jmsTemplateFile.convertAndSend(message);
            logger.info("Put attachment on queue.");
        }
    }
}
