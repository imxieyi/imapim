package imapim.test.protocol;

import imapim.data.Email;
import imapim.protocol.SMTPHelper;
import org.junit.jupiter.api.Test;

import javax.mail.MessagingException;

public class SMTPTest {

    @Test
    void testSendMail() throws MessagingException {
        Email email = new Email();
        email.from = "im@lattepanda";
        email.to.add("xieyi@lattepanda");
        email.subject = "java test";
        email.content = "java test";
        SMTPHelper.getInstance().send(email);
    }

}
