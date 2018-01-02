package imapim.protocol;

import imapim.data.Email;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileInputStream;
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

    private Properties props = new Properties();
    private String user;
    private String password;
    private Session session;
    private Transport transport;

    private SMTPHelper() {
        Properties config = new Properties();
        try {
            config.load(new FileInputStream(new File("config.properties")));
            props.setProperty("mail.transport.protocol", "smtp");
            if(config.getProperty("smtpssl").equals("true")) {
                props.put("mail.smtp.socketFactory.port", config.getProperty("smtpport"));
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            }
            props.setProperty("mail.smtp.host", config.getProperty("smtphost"));
            props.setProperty("mail.smtp.port", config.getProperty("smtpport"));
            props.setProperty("mail.smtp.auth", "true");
            props.setProperty("mail.debug", config.getProperty("debug"));
            user = config.getProperty("user");
            password = config.getProperty("password");
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
