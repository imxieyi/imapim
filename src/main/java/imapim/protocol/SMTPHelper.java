package imapim.protocol;

import imapim.data.Email;
import imapim.data.Setting;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

public class SMTPHelper {
    private static SMTPHelper ourInstance = null;

    public static synchronized SMTPHelper getInstance() {
        if(ourInstance == null) {
            ourInstance = new SMTPHelper();
        }
        return ourInstance;
    }

    private Properties props;
    private String user;
    private String password;
    private Session session;
    private Transport transport;

    private SMTPHelper() {
        reload();
    }

    public void reload() {
        if (Setting.instance == null) {
            return;
        }
        props = new Properties();
        try {
            props.setProperty("mail.transport.protocol", "smtp");
            if (Setting.instance.optBoolean("smtpssl", false)) {
                props.put("mail.smtp.socketFactory.port", Setting.instance.optString("smtpport", "465"));
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            }
            props.setProperty("mail.smtp.host", Setting.instance.optString("smtphost"));
            props.setProperty("mail.smtp.port", Setting.instance.optString("smtpport", "465"));
            props.setProperty("mail.smtp.auth", "true");
            props.setProperty("mail.debug", Setting.instance.optBoolean("debug", false) ? "true" : "false");
            user = Setting.instance.optString("user");
            password = Setting.instance.optString("password");
            session = Session.getInstance(props);
            transport = session.getTransport();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void send(Email mail) throws MessagingException {
//        Session session = Session.getInstance(props);
        // Compose email
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(mail.from));
        message.setSubject(mail.subject);
        for(String addr : mail.to) {
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(addr));
        }
        if(mail.timestamp == null) {
            mail.timestamp = new Date();
        }
        message.setSentDate(mail.timestamp);
        message.setText(mail.content);
        message.saveChanges();
        // Send email
//        Transport transport = session.getTransport();
        transport.connect(user, password);
        transport.sendMessage(message, message.getAllRecipients());
        transport.close();
    }

}
