/*
 * All Application code is Copyright 2016, The Department of Homeland Security (DHS), U.S. Customs and Border Protection (CBP).
 *
 * Please see LICENSE.txt for details.
 */
package gov.gtas.flowobjects;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AttachmentFilterTest {

    @Mock
    MimeMessage mimeMessage;

    @Mock
    MimeMultipart mimeMultipart;

    @Test
    public void filterPositiveTest() throws IOException, MessagingException {
        AttachmentFilter attachmentFilter = new AttachmentFilter();
        mimeMessage = mock(MimeMessage.class);
        mimeMultipart = mock(MimeMultipart.class);
        when(mimeMessage.getContent()).thenReturn(mimeMultipart);
        Assert.assertTrue(attachmentFilter.filter(mimeMessage));
    }

    @Test
    public void filterNegativeTest() throws IOException, MessagingException {
        AttachmentFilter attachmentFilter = new AttachmentFilter();
        mimeMessage = mock(MimeMessage.class);
        Object notAMimeMultipart = new Object();
        when(mimeMessage.getContent()).thenReturn(notAMimeMultipart);
        Assert.assertFalse(attachmentFilter.filter(mimeMessage));
    }

}
