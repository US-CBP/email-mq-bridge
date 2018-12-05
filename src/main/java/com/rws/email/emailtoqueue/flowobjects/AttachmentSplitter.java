package com.rws.email.emailtoqueue.flowobjects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.Splitter;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class AttachmentSplitter {

    private Logger logger = LoggerFactory.getLogger(AttachmentSplitter.class);

    @Splitter
    public List<Message<?>> extractAttachmentList(@Payload Attachments attachments) {

        List<Message<?>> jmsMessages = new ArrayList<>();
        Map<String, String> attachmentMap = attachments.getAttachmentsMappedByName();
        for (String attachmentName : attachmentMap.keySet()) {
            String payload = attachmentMap.get(attachmentName);
            Message message = MessageBuilder
                    .withPayload(payload)
                    .setHeader("filename", attachmentName)
                    .build();
            jmsMessages.add(message);
            logger.info("Created message for " + attachmentName);
        }
        return jmsMessages;
    }
}
