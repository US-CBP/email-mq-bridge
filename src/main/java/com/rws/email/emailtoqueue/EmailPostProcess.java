package com.rws.email.emailtoqueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
    public void success(MimeMessage message) {
        logger.info("success!");
        //    copyMessageToFolder(message, successFolder);
    }

    @SuppressWarnings("unused")
    public void failure(MimeMessage message) {
        logger.info("Error processing message - putting in error folder");
        //    copyMessageToFolder(message, failureFolder);
    }


/*
    private void copyMessageToFolder(MimeMessage message, String folderName) throws MessagingException {
        Folder folder = message.getFolder();
        Message[] messageAsArray = new Message[]{message};
        if (!folder.isOpen()) {
            folder.open(Folder.READ_ONLY);
        }
        Store emailStore = folder.getStore();
        Folder outFolder = emailStore.getFolder(folderName);
        if (!outFolder.isOpen()) {
            outFolder.open(Folder.READ_WRITE);
        }
//        folder.copyMessages(messageAsArray, outFolder);
        outFolder.appendMessages(messageAsArray);
        outFolder.close(true);
        folder.close(true);
    }

    */
}
