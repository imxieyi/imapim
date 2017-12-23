package imapim.data;

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
}
