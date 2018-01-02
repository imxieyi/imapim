package imapim.test.protocol;

import imapim.data.Email;
import imapim.protocol.IMAPHelper;
import org.junit.jupiter.api.Test;

import javax.mail.MessagingException;
import java.util.ArrayList;

public class IMAPTest {

    @Test
    void testReceive() throws MessagingException {
        ArrayList<Email> emails = IMAPHelper.getInstance().getMails();
        for(Email e : emails) {
            System.out.println(e);
        }
        System.out.println("Got " + emails.size() + " emails");
    }

    static void main(String[] args) {
        IMAPHelper.getInstance().addObserver((o, arg) -> {
            Email e = (Email) arg;
            System.out.println(e);
        });
        IMAPHelper.getInstance().startListening();
    }
}
