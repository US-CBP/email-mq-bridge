package com.rws.email.emailtoqueue;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.mail.ImapIdleChannelAdapter;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class InboundEmailHandler {

    private Logger logger = LoggerFactory.getLogger(InboundEmailHandler.class);

    @Value("${email.local.save}")
    Boolean SAVE_ATTACHMENTS_LOCALLY;

    @Value("${email.local.folder.attachments.saved}")
    String FOLDER_WHERE_ATTACHMENTS_ARE_SAVED;

    @Value("${email.local.maxFilenameLength}")
    Integer MAX_FILE_NAME_LENGTH;

    private final
    ImapIdleChannelAdapter imapIdleChannelAdapter;

    private final
    OutboundQueue outboundQueue;

    @Autowired
    public InboundEmailHandler(@Qualifier("inboundMessageAdapter") ImapIdleChannelAdapter imapIdleChannelAdapter,
                               OutboundQueue outboundQueue) {
        this.imapIdleChannelAdapter = imapIdleChannelAdapter;
        this.outboundQueue = outboundQueue;
    }

    @Bean
    IntegrationFlow pollingFlow() {
        return IntegrationFlows
                .from(imapIdleChannelAdapter)
                .handle(this::processEmailAndPutOnOutboundQueue)
                .get();
    }

    private void processEmailAndPutOnOutboundQueue(Message<?> message) {
        MimeMessage mimeMessage = (MimeMessage) message.getPayload();
        Map<String, String> attachmentNameAsKeyAttachmentAsValue = saveAttachmentsToDiskAndReturnAttachmentsAsStringMappedByFileName(mimeMessage);
        outboundQueue.putAttachmentsOnQueue(attachmentNameAsKeyAttachmentAsValue);
    }

    private Map<String, String> saveAttachmentsToDiskAndReturnAttachmentsAsStringMappedByFileName(MimeMessage mimeMessage) {
        Map<String, String> attachmentAndName = new HashMap<>();
        try {
            Multipart multiPart = (Multipart) mimeMessage.getContent();
            for (int i = 0; i < multiPart.getCount(); i++) {
                try {
                    MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(i);
                    if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                        String fileName = getFileName(part);
                        String attachmentAsString = IOUtils.toString(part.getInputStream(), StandardCharsets.UTF_8);
                        attachmentAndName.put(fileName, attachmentAsString);
                        if (SAVE_ATTACHMENTS_LOCALLY) {
                            String path = FOLDER_WHERE_ATTACHMENTS_ARE_SAVED + fileName;
                            part.saveFile(path);
                            logger.info("Saved file to directory with path: " + path);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error while processing MimeBodyPart/attachment part from message!", e);
                }
            }
        } catch (MessagingException | IOException e) {
            logger.error("Error processing message!", e);
        }
        return attachmentAndName;
    }

    private String getFileName(MimeBodyPart part) throws MessagingException {
        String fileName = "" + part.hashCode() + UUID.randomUUID() + part.getFileName();
        if (FOLDER_WHERE_ATTACHMENTS_ARE_SAVED.length() + fileName.length() >= MAX_FILE_NAME_LENGTH) {
            fileName = fileName.substring(0, MAX_FILE_NAME_LENGTH);
        }
        return fileName;
    }
}
