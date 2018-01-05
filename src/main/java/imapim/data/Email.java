package imapim.data;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.ArrayList;
import java.util.Date;

public class Email {
    public String from;
    public ArrayList<String> to = new ArrayList<>();
    public String subject;
    public String content;
    public Date timestamp;

    @Override
    public String toString() {
        String sb = ("Time: " + timestamp.toString() + "\n") +
                "From: " + from + "\n" +
                "To: " + to + "\n" +
                "Subject: " + subject + "\n" +
                "Content:\n" + content + "\n";
        return sb;
    }

    public static boolean validate(String email) {
        boolean result = true;
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException ex) {
            result = false;
        }
        return result;
    }

}
