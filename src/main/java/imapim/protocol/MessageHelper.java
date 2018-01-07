package imapim.protocol;

import imapim.data.ChatMessage;
import imapim.data.Email;
import imapim.data.Message;
import imapim.data.Setting;
import imapim.security.PGPDecrypt;
import imapim.security.PGPEncrypt;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class MessageHelper extends Observable implements Observer {

    private static Logger log = Logger.getLogger(MessageHelper.class.getName());

    private static MessageHelper outInstance;

    public static synchronized MessageHelper getInstance() {
        if(outInstance == null) {
            try {
                outInstance = new MessageHelper();
            } catch (IOException e) {
                log.severe("Failed to load config: " + e.getMessage());
            } catch (PGPException e) {
                log.severe("Failed to initialize encryption: " + e.getMessage());
            }
        }
        return outInstance;
    }

    private PGPDecrypt decrypt;
    private Map<String, PGPPublicKey> keys = new HashMap<>();

    public void addKey(String addr, String path, String id) throws IOException, PGPException {
        if (keys.containsKey(addr)) {
            keys.remove(addr);
        }
        keys.put(addr, PGPEncrypt.getPublicKey(path, id));
    }

    private MessageHelper() throws IOException, PGPException {
        reload();
    }

    public void reload() throws IOException, PGPException {
        if (Setting.instance == null) {
            return;
        }
        // Initialize encryption and decryption
        decrypt = new PGPDecrypt();
        decrypt.loadPrivateKey(Setting.instance.optString("privatekeyFile"),
                Setting.instance.optString("privatekeyId"),
                Setting.instance.optString("privatekeyPass"));
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
                notifyEmail(e);
            }
        } catch (MessagingException e) {
            log.warning("Failed to get old unread messages");
        }
        IMAPHelper.getInstance().addObserver((o, arg) -> {
            Email e = (Email) arg;
            notifyEmail(e);
        });
        SendMailQueue.getInstance().addObserver(this);
        IMAPHelper.getInstance().startListening();
        SendMailQueue.getInstance().start();
    }

    private void notifyEmail(Email e) {
        try {
            setChanged();
            ChatMessage cm = new ChatMessage();
            cm.mail = e;
            cm.message = parseEmail(e);
            cm.sent = false;
            notifyObservers(cm);
        } catch (Exception e1) {
            log.warning("Failed to decrypt a message: " + e1.getMessage());
        }
    }

    public void stop() {
        IMAPHelper.getInstance().stopListening();
        IMAPHelper.getInstance().deleteObservers();
        SendMailQueue.getInstance().deleteObserver(this);
        SendMailQueue.getInstance().stop();
    }

    public void send(Message m) throws IOException, PGPException {
        PGPEncrypt encrypt = new PGPEncrypt();
        for (String addr : m.to) {
            encrypt.addPublicKey(keys.get(addr));
        }
        byte[] encrypted = encrypt.encrypt(m.content.getBytes("UTF-8"), true, true);
        Email e = new Email();
        e.to = m.to;
        e.subject = "IM Message";
        e.from = m.from;
        e.timestamp = m.timestamp;
        e.content = new String(encrypted);
        ChatMessage cm = new ChatMessage();
        cm.mail = e;
        cm.message = m;
        cm.sent = true;
        SendMailQueue.getInstance().add(cm);
    }

    @Override
    public void update(Observable o, Object arg) {
        setChanged();
        notifyObservers(arg);
    }
}
