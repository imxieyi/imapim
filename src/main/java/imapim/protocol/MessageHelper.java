package imapim.protocol;

import imapim.data.Email;
import imapim.data.Message;
import imapim.security.PGPDecrypt;
import imapim.security.PGPEncrypt;
import org.bouncycastle.openpgp.PGPException;

import javax.mail.MessagingException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Properties;
import java.util.logging.Logger;

public class MessageHelper extends Observable {

    private static Logger log = Logger.getLogger(MessageHelper.class.getName());

    private static MessageHelper outInstance;

    public static synchronized MessageHelper getInstance() {
        if(outInstance == null) {
            try {
                outInstance = new MessageHelper();
            } catch (IOException e) {
                log.severe("Failed to load config: " + e.getMessage());
                System.exit(-1);
            } catch (PGPException e) {
                log.severe("Failed to initialize encryption: " + e.getMessage());
                System.exit(-1);
            }
        }
        return outInstance;
    }

    private PGPDecrypt decrypt;
    private PGPEncrypt encrypt;

    private MessageHelper() throws IOException, PGPException {
        // Load settings
        Properties prop = new Properties();
        prop.load(new FileInputStream(new File("config.properties")));
        // Initialize encryption and decryption
        encrypt = new PGPEncrypt();
        encrypt.loadPublicKey(prop.getProperty("pubkeyfile"), prop.getProperty("pubkeyid"));
        decrypt = new PGPDecrypt();
        decrypt.loadPrivateKey(prop.getProperty("prikeyfile"), prop.getProperty("prikeyid"), prop.getProperty("prikeypass"));
    }

    private Message parseEmail(Email e) throws IOException, PGPException {
        byte[] decrypted = decrypt.decrypt(e.content.getBytes());
        Message m = new Message();
        m.content = new String(decrypted, "UTF-8");
        m.timestamp = e.timestamp;
        m.from = e.from;
        m.to = e.to;
        return m;
    }

    public void start() {
        // Get old unread messages
        try {
            ArrayList<Email> old = IMAPHelper.getInstance().getMails();
            for(Email e : old) {
                try {
                    setChanged();
                    notifyObservers(parseEmail(e));
                } catch (Exception e1) {
                    log.warning("Failed to decrypt a message: " + e1.getMessage());
                }
            }
        } catch (MessagingException e) {
            log.warning("Failed to get old unread messages");
        }
        IMAPHelper.getInstance().addObserver((o, arg) -> {
            Email e = (Email) arg;
            try {
                setChanged();
                notifyObservers(parseEmail(e));
            } catch (Exception e1) {
                log.warning("Failed to decrypt a message: " + e1.getMessage());
            }
        });
        IMAPHelper.getInstance().startListening();
    }

    public void stop() {
        IMAPHelper.getInstance().stopListening();
        IMAPHelper.getInstance().deleteObservers();
    }

    public void send(Message m) throws IOException, PGPException, MessagingException {
        byte[] encrypted = encrypt.encrypt(m.content.getBytes("UTF-8"), true, true);
        Email e = new Email();
        e.to = m.to;
        e.subject = "IM Message";
        e.from = m.from;
        e.timestamp = m.timestamp;
        e.content = new String(encrypted);
        SMTPHelper.getInstance().send(e);
    }

}
