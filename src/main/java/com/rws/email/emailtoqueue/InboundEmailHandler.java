package com.rws.email.emailtoqueue;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.mail.MailReceivingMessageSource;
import org.springframework.integration.transaction.DefaultTransactionSynchronizationFactory;
import org.springframework.integration.transaction.ExpressionEvaluatingTransactionSynchronizationProcessor;
import org.springframework.integration.transaction.PseudoTransactionManager;
import org.springframework.integration.transaction.TransactionSynchronizationFactory;
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
import java.util.concurrent.TimeUnit;

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
    MailReceivingMessageSource mailReceivingMessageSource;
    private final
    TaskExecutor taskExecutor;
    private final
    ApplicationContext applicationContext;

    private final
    OutboundQueue outboundQueue;
    @Value("${email.maxProcessedMessagesPerPoll}")
    Integer maxThreadsPerPoll;
    @Value("${email.pollTimeInSeconds}")
    Integer pollTimeInSeconds;

    @Autowired
    public InboundEmailHandler(@Qualifier("inboundMessageAdapter") MailReceivingMessageSource mailReceivingMessageSource,
                               @Qualifier("emailTaskExecutor") TaskExecutor taskExecutor,
                               OutboundQueue outboundQueue,
                               ApplicationContext applicationContext) {
        this.mailReceivingMessageSource = mailReceivingMessageSource;
        this.taskExecutor = taskExecutor;
        this.outboundQueue = outboundQueue;
        this.applicationContext = applicationContext;
    }

    @Bean
    IntegrationFlow pollingFlow() {
        return IntegrationFlows
                .from(mailReceivingMessageSource, inboundMailConfig -> inboundMailConfig
                        .poller(
                                Pollers.fixedRate(pollTimeInSeconds, TimeUnit.SECONDS)
                                        .taskExecutor(taskExecutor)
                                        .maxMessagesPerPoll(maxThreadsPerPoll)
                                        .transactionSynchronizationFactory(transactionSynchronizationFactory())
                                        .transactional(transactionManager())
                        )
                )
                .handle(this::processEmailAndPutOnOutboundQueue)
                .get();
    }

    @Bean
    PseudoTransactionManager transactionManager() {
        return new PseudoTransactionManager();
    }

    @Bean
    @Qualifier("transactionFactory")
    TransactionSynchronizationFactory transactionSynchronizationFactory() {
        ExpressionParser parser = new SpelExpressionParser();
        ExpressionEvaluatingTransactionSynchronizationProcessor syncProcessor =
                new ExpressionEvaluatingTransactionSynchronizationProcessor();
        syncProcessor.setBeanFactory(applicationContext.getAutowireCapableBeanFactory());
        syncProcessor.setAfterCommitExpression(parser.parseExpression("@emailPostProcess.success(payload)"));
        syncProcessor.setAfterRollbackExpression(parser.parseExpression("@emailPostProcess.failure(payload)"));
        return new DefaultTransactionSynchronizationFactory(syncProcessor);
    }

    private void processEmailAndPutOnOutboundQueue(Message<?> message) {
        try {
            MimeMessage mimeMessage = (MimeMessage) message.getPayload();
            Map<String, String> attachmentNameAsKeyAttachmentAsValue = saveAttachmentsToDiskAndReturnAttachmentsAsStringWithFileNameKey(mimeMessage);
            outboundQueue.putAttachmentsOnQueue(attachmentNameAsKeyAttachmentAsValue);
        } catch (MessagingException | IOException e) {
            throw new EmailProcessingException("Failed to process message, will attempt to put on failure queue", e);
        }
    }

    private Map<String, String> saveAttachmentsToDiskAndReturnAttachmentsAsStringWithFileNameKey(MimeMessage mimeMessage) throws MessagingException, IOException {
        Map<String, String> attachmentAndName = new HashMap<>();
        Multipart multiPart = (Multipart) mimeMessage.getContent();
        for (int i = 0; i < multiPart.getCount(); i++) {
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
