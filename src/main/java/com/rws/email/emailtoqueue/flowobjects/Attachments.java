package com.rws.email.emailtoqueue.flowobjects;

import lombok.Data;

import javax.mail.internet.MimeMessage;
import java.util.Map;

@Data
class Attachments {

    private Map<String, String> attachmentsMappedByName;
    private MimeMessage email;

    Attachments(Map<String, String> attachmentsMappedByName, MimeMessage email) {
        this.attachmentsMappedByName = attachmentsMappedByName;
        this.email = email;
    }
}
