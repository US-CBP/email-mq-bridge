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
            try {
                String payload = attachmentNameAsKeyAttachmentAsValue.get(attachmentName);
                payload = stripNewLineAndCarriageReturn(payload);
                Message message  = MessageBuilder
                        .withPayload(payload)
                        .setHeader("filename", attachmentName)
                        .build();
                jmsTemplateFile.convertAndSend(message);
                logger.info("Put attachment on queue.");
            } catch (Exception ex) {
                logger.error("Error forwarding message", ex);
            }
        }
    }

    private String stripNewLineAndCarriageReturn(String attachment) {
        attachment = attachment.replaceAll("\\n", "");
        attachment = attachment.replaceAll("\\r", "");
        return attachment;
    }
}
