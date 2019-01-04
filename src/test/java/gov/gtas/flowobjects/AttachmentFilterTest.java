/*
 * All Application code is Copyright 2016, The Department of Homeland Security (DHS), U.S. Customs and Border Protection (CBP).
 *
 * Please see LICENSE.txt for details.
 */
package gov.gtas.flowobjects;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AttachmentFilterTest {

    @Mock
    MimeMessage mimeMessage;

    @Mock
    MimeMultipart mimeMultipart;

    @Test
    public void mimeMultipartContentPositive() throws IOException, MessagingException {
        AttachmentFilter attachmentFilter = new AttachmentFilter();
        attachmentFilter.setWhitelistOn(false);
        mimeMessage = mock(MimeMessage.class);
        mimeMultipart = mock(MimeMultipart.class);
        when(mimeMessage.getContent()).thenReturn(mimeMultipart);
        Assert.assertTrue(attachmentFilter.filter(mimeMessage));
    }

    @Test
    public void whitelistEmailNoSender() throws IOException, MessagingException {
        AttachmentFilter attachmentFilter = new AttachmentFilter();
        attachmentFilter.setWhitelistOn(true);
        mimeMessage = mock(MimeMessage.class);
        mimeMultipart = mock(MimeMultipart.class);
        when(mimeMessage.getContent()).thenReturn(mimeMultipart);
        Assert.assertFalse(attachmentFilter.filter(mimeMessage));
    }

    @Test
    public void whitelistEmailAddressNegative() throws IOException, MessagingException {
        AttachmentFilter attachmentFilter = new AttachmentFilter();
        attachmentFilter.setWhitelistOn(true);
        List<String> whitelistedEmails = new ArrayList<>();
        whitelistedEmails.add("not gonna work@faaaaail.fake");
        attachmentFilter.setWhitelistedEmails(whitelistedEmails);
        InternetAddress internetAddress = new InternetAddress();
        internetAddress.setAddress("foobar@foo.com");
        Address [] fromArray = new Address[] {internetAddress};
        mimeMessage = mock(MimeMessage.class);
        mimeMultipart = mock(MimeMultipart.class);
        when(mimeMessage.getFrom()).thenReturn(fromArray);
        when(mimeMessage.getContent()).thenReturn(mimeMultipart);
        Assert.assertFalse(attachmentFilter.filter(mimeMessage));
    }

    @Test
    public void whitelistEmailPositiveTest() throws IOException, MessagingException {
        AttachmentFilter attachmentFilter = new AttachmentFilter();
        attachmentFilter.setWhitelistOn(true);
        List<String> whitelistedEmails = new ArrayList<>();
        whitelistedEmails.add("fOobar@FOO.fake");
        attachmentFilter.setWhitelistedEmails(whitelistedEmails);
        InternetAddress internetAddress = new InternetAddress();
        internetAddress.setAddress("foobar@foo.fake");
        Address [] fromArray = new Address[] {internetAddress, null, null};
        mimeMessage = mock(MimeMessage.class);
        mimeMultipart = mock(MimeMultipart.class);
        when(mimeMessage.getFrom()).thenReturn(fromArray);
        when(mimeMessage.getContent()).thenReturn(mimeMultipart);
        Assert.assertTrue(attachmentFilter.filter(mimeMessage));
    }

    @Test
    public void whiteListPositiveSenders() throws IOException, MessagingException {
        AttachmentFilter attachmentFilter = new AttachmentFilter();
        attachmentFilter.setWhitelistOn(true);
        List<String> whitelistedEmails = new ArrayList<>();
        whitelistedEmails.add("fOobar@FOO.fake");
        attachmentFilter.setWhitelistedEmails(whitelistedEmails);
        InternetAddress internetAddress = new InternetAddress();
        internetAddress.setAddress("foobar@foo.fake");
        attachmentFilter.setWhitelistedEmails(whitelistedEmails);
        InternetAddress internetAddress2 = new InternetAddress();
        internetAddress2.setAddress("bademailaddress@bademailaddresses.fake");
        Address [] fromArray = new Address[] {internetAddress2, null, internetAddress};
        mimeMessage = mock(MimeMessage.class);
        mimeMultipart = mock(MimeMultipart.class);
        when(mimeMessage.getFrom()).thenReturn(fromArray);
        when(mimeMessage.getContent()).thenReturn(mimeMultipart);
        Assert.assertTrue(attachmentFilter.filter(mimeMessage));
    }


}
