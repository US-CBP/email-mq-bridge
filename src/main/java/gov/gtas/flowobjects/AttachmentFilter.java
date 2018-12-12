/*
 * All Application code is Copyright 2016, The Department of Homeland Security (DHS), U.S. Customs and Border Protection (CBP).
 *
 * Please see LICENSE.txt for details.
 */
package gov.gtas.flowobjects;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.integration.annotation.Filter;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@Component
@PropertySource("classpath:application.yml")
@ConfigurationProperties(prefix = "filter.config")
public class AttachmentFilter {

    private final Logger logger = LoggerFactory.getLogger(AttachmentFilter.class);
    private List<String> whitelistedEmails;
    private Boolean whitelistOn;

    @Filter
    public boolean filter(@Payload MimeMessage payload) throws MessagingException, IOException {
        logger.info("Message being filtered!");
        boolean isMultipartMessage = isaMimeMulitpartPayload(payload);
        boolean isWhiteListedSender = isWhiteListedSender(payload);

        return isMultipartMessage && isWhiteListedSender;
    }


    private boolean isaMimeMulitpartPayload(@Payload MimeMessage payload) throws IOException, MessagingException {
        boolean isMultipartMessage = payload.getContent() instanceof Multipart;
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

    private boolean isWhiteListedSender(@Payload MimeMessage payload) {
        boolean isWhiteListedSender = true;
        if (whitelistOn) {
            isWhiteListedSender = isFromTrustedSender(payload);
        }
        return isWhiteListedSender;
    }

    private boolean isFromTrustedSender(MimeMessage payload) {
        List<String> addressOfEmailSender = new ArrayList<>();
        boolean allowedEmailAddress = false;
        try {
            for (Address address : payload.getFrom()) {
                if (address instanceof InternetAddress) {
                    InternetAddress internetAddress = (InternetAddress) address;
                    String sender = internetAddress.getAddress();
                    addressOfEmailSender.add(sender);
                    for (String whitelistEmail : whitelistedEmails) {
                        if (whitelistEmail.equalsIgnoreCase(sender)) {
                            allowedEmailAddress = true;
                            break;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
            //ignored - if it fails to parse it won't be a valid message.
        }

        if (!allowedEmailAddress) {
            String emailConcat = Arrays.toString(addressOfEmailSender.toArray());
            logger.info("Filtering out non-whitelisted message from email addresses : " + emailConcat);
        }
        return allowedEmailAddress;
    }

}
