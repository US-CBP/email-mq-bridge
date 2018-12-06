package com.rws.email.emailtoqueue.flowobjects;

import lombok.Data;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Data
@Component
@PropertySource("classpath:application.yml")
public class AttachmentTransformer {

    @Value("${email.local.save}")
    Boolean SAVE_ATTACHMENTS_LOCALLY;

    @Value("${email.local.folder.attachments.saved}")
    String FOLDER_WHERE_ATTACHMENTS_ARE_SAVED;

    @Value("${email.local.maxFilenameLength}")
    Integer MAX_FILE_NAME_LENGTH;

    private Logger logger = LoggerFactory.getLogger(AttachmentTransformer.class);

    @Transformer
    public List<Message<?>> transform(MimeMessage payload) throws IOException, MessagingException {
        Map<String, String> attachmentMap = attachmentsAsStringWithFileNameAsKey(payload);
        return createMessages(attachmentMap);
    }

    private Map<String, String> attachmentsAsStringWithFileNameAsKey(MimeMessage mimeMessage) throws MessagingException, IOException {
        Map<String, String> attachmentAndName = new HashMap<>();
        if (mimeMessage.getContent() instanceof Multipart) {
            Multipart multiPart = (Multipart) mimeMessage.getContent();
            for (int i = 0; i < multiPart.getCount(); i++) {
                MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(i);
                if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                    String fileName = getFileName(part);
                    String attachmentAsString = IOUtils.toString(part.getInputStream(), StandardCharsets.UTF_8);
                    attachmentAndName.put(fileName, attachmentAsString);
                    if (SAVE_ATTACHMENTS_LOCALLY) {
                        String path = FOLDER_WHERE_ATTACHMENTS_ARE_SAVED + fileName.replaceAll("\\\\", "");
                        part.saveFile(path);
                        logger.info("Saved file to directory with path: " + path);
                    }
                }
            }
        }
        return attachmentAndName;
    }

    private String getFileName(MimeBodyPart part) throws MessagingException {
        String fileName = "" + part.hashCode() + UUID.randomUUID() + part.getFileName();
        if (SAVE_ATTACHMENTS_LOCALLY &&
                FOLDER_WHERE_ATTACHMENTS_ARE_SAVED.length() + fileName.length() >= MAX_FILE_NAME_LENGTH) {
            fileName = fileName.substring(0, MAX_FILE_NAME_LENGTH);
        }
        return fileName;
    }

    private List<Message<?>> createMessages(Map<String, String> attachmentMap) {
        List<Message<?>> jmsMessages = new ArrayList<>();
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
