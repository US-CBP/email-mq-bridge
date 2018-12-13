/*
 * All Application code is Copyright 2016, The Department of Homeland Security (DHS), U.S. Customs and Border Protection (CBP).
 *
 * Please see LICENSE.txt for details.
 */
package gov.gtas.flowobjects;

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

import javax.mail.BodyPart;
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
        logger.info("Transforming message!");
        Map<String, String> attachmentMap = attachmentsAsStringWithFileNameAsKey(payload);
        return createMessages(attachmentMap);
    }

    private Map<String, String> attachmentsAsStringWithFileNameAsKey(MimeMessage payload) throws MessagingException, IOException {
        Multipart multiPart = (Multipart) payload.getContent();
        Map<String, String> attachmentAndName = new HashMap<>();
        attachmentAndName = extractAttachments(multiPart, attachmentAndName);
        return attachmentAndName;
    }

    private Map<String, String> extractAttachments(Multipart multiPart, Map<String, String> attachmentAndName) {
        try {
            for (int i = 0; i < multiPart.getCount(); i++) {
                BodyPart part = multiPart.getBodyPart(i);
                if (part.getContent() instanceof Multipart) {
                    attachmentAndName = extractAttachments((Multipart) part.getContent(), attachmentAndName);
                } else if (part instanceof MimeBodyPart) {
                    MimeBodyPart mimeBodyPart = (MimeBodyPart) part;
                    String fileName = createUniqueFileName(mimeBodyPart.getFileName());
                    if (Part.ATTACHMENT.equalsIgnoreCase(mimeBodyPart.getDisposition())) {
                        String attachmentAsString = IOUtils.toString(mimeBodyPart.getInputStream(), StandardCharsets.UTF_8);
                        attachmentAndName.put(createUniqueFileName(fileName), attachmentAsString);
                        if (SAVE_ATTACHMENTS_LOCALLY) {
                            saveFile(mimeBodyPart, fileName);
                        }
                    }
                }
            }
        } catch (IOException | MessagingException e) {
            logger.error("Failed to process attachment. Continuing to process other message parts in search of attachments...");
        }
        return attachmentAndName;
    }

    private void saveFile(MimeBodyPart part, String fileName) throws MessagingException {
        String path = FOLDER_WHERE_ATTACHMENTS_ARE_SAVED + fileName.replaceAll("\\\\", "");
        try {
            part.saveFile(path);
            logger.info("Saved file to directory with path: " + path);
        } catch (IOException io) {
            logger.error("FAILED TO WRITE FILE TO PATH. IS SAVE FOLDER CORRECTLY SET?: " + path);
        }
    }

    private String createUniqueFileName(String name) {
        String fileName = UUID.randomUUID() + name;
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
