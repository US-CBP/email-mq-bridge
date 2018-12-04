package com.rws.email.emailtoqueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;

/*
 * Methods below are used by the transactionSynchronizationFactory.
 * */
@Component
public class EmailPostProcess {

    @Value("${email.folder.success}")
    String successFolder;
    @Value("${email.folder.failure}")
    String failureFolder;
    private Logger logger = LoggerFactory.getLogger(EmailPostProcess.class);

    @SuppressWarnings("unused")
    public void success(MimeMessage message) throws Exception {
        copyMessageToFolder(message, successFolder);
    }

    @SuppressWarnings("unused")
    public void failure(MimeMessage message) throws MessagingException {
        logger.info("Error processing message - putting in error folder");
        copyMessageToFolder(message, failureFolder);
    }

    private void copyMessageToFolder(MimeMessage message, String folderName) throws MessagingException {
        Folder folder = message.getFolder();
        Message[] messageAsArray = new Message[]{message};
        folder.open(Folder.READ_ONLY);
        Store emailStore = folder.getStore();
        Folder outFolder = emailStore.getFolder(folderName);
        outFolder.open(Folder.READ_WRITE);
        folder.copyMessages(messageAsArray, outFolder);
        outFolder.appendMessages(messageAsArray);
        outFolder.close(true);
        folder.close(true);
    }
}
