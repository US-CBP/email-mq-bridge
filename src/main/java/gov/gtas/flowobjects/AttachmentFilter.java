/*
 * All Application code is Copyright 2016, The Department of Homeland Security (DHS), U.S. Customs and Border Protection (CBP).
 *
 * Please see LICENSE.txt for details.
 */
package gov.gtas.flowobjects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.Filter;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;
import java.io.IOException;


@Component
public class AttachmentFilter {

    private Logger logger = LoggerFactory.getLogger(AttachmentFilter.class);

    @Filter
    public boolean filter(@Payload MimeMessage payload) throws MessagingException, IOException {
        boolean isMultipartMessage = (payload.getContent() instanceof Multipart);
        if (!isMultipartMessage) {
            logNonMultipartMessage(payload);
        }
        return isMultipartMessage;
    }

    private void logNonMultipartMessage(@Payload MimeMessage payload) {
        try {
            String subject = payload.getSubject();
            logger.info("Ignoring non-multipart message with subject: " + subject);
        } catch (Exception ignored) {
            logger.info("Ignoring non-multipart message. Failed to extract subject.");
        }
    }
}
