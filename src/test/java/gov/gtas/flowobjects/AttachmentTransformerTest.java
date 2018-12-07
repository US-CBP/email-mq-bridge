/*
 * All Application code is Copyright 2016, The Department of Homeland Security (DHS), U.S. Customs and Border Protection (CBP).
 *
 * Please see LICENSE.txt for details.
 */
package gov.gtas.flowobjects;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.messaging.Message;

import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AttachmentTransformerTest {

    private static final String PAYLOAD_ONE = "mime1";
    private static final String PAYLOAD_TWO = "mime2";
    @Mock
    MimeMessage mimeMessage;

    @Mock
    MimeMultipart mimeMultipart;

    @Mock
    MimeBodyPart mimeBodyPart;

    @Mock
    MimeBodyPart mimeBodyPart2;

    private AttachmentTransformer attachmentTransformer;

    @Before
    public void before() throws IOException, MessagingException {
        setUpMocks();
        attachmentTransformer = new AttachmentTransformer();
        attachmentTransformer.setFOLDER_WHERE_ATTACHMENTS_ARE_SAVED("UNUSED");
        attachmentTransformer.setMAX_FILE_NAME_LENGTH(255);
        attachmentTransformer.setSAVE_ATTACHMENTS_LOCALLY(true);
    }

    private void setUpMocks() throws IOException, MessagingException {
        mimeMessage = mock(MimeMessage.class);
        mimeMultipart = mock(MimeMultipart.class);
        mimeBodyPart = mock(MimeBodyPart.class);
        mimeBodyPart2 = mock(MimeBodyPart.class);
        when(mimeMultipart.getCount()).thenReturn(2);
        when(mimeMultipart.getBodyPart(0)).thenReturn(mimeBodyPart);
        when(mimeMultipart.getBodyPart(1)).thenReturn(mimeBodyPart2);
        when(mimeBodyPart.getDisposition()).thenReturn(Part.ATTACHMENT);
        when(mimeBodyPart2.getDisposition()).thenReturn(Part.ATTACHMENT);
        when(mimeBodyPart.getFileName()).thenReturn("foo");
        when(mimeBodyPart2.getFileName()).thenReturn("bar");
        doNothing().when(mimeBodyPart).saveFile(anyString());
        doNothing().when(mimeBodyPart2).saveFile(anyString());
        when(mimeBodyPart.getInputStream()).thenReturn(IOUtils.toInputStream(PAYLOAD_ONE));
        when(mimeBodyPart2.getInputStream()).thenReturn(IOUtils.toInputStream(PAYLOAD_TWO));
        when(mimeMessage.getContent()).thenReturn(mimeMultipart);
    }

    @Test
    public void emailContentsAccurate() throws IOException, MessagingException {
        List<Message<?>> messages = attachmentTransformer.transform(mimeMessage);
        List<String> acceptedMessages = Arrays.asList(PAYLOAD_ONE, PAYLOAD_TWO);
        String payloadOne = (String) messages.get(0).getPayload();
        String payloadTwo = (String) messages.get(1).getPayload();
        Assert.assertTrue(acceptedMessages.contains(payloadOne));
        Assert.assertTrue(acceptedMessages.contains(payloadTwo));

    }

    @Test
    public void MultipleAttachmentsCreateMultipleMessages() throws IOException, MessagingException {
        List<Message<?>> messages = attachmentTransformer.transform(mimeMessage);
        Assert.assertEquals(2, messages.size());
    }

}
