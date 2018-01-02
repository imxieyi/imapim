package imapim.protocol;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.util.MailSSLSocketFactory;
import imapim.data.Email;

import javax.mail.*;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.search.FlagTerm;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;
import java.util.Properties;
import java.util.logging.Logger;

public class IMAPHelper extends Observable {

    private static Logger log = Logger.getLogger(IMAPHelper.class.getName());

    private static IMAPHelper ourInstance = null;

    public static synchronized IMAPHelper getInstance() {
        if(ourInstance == null) {
            ourInstance = new IMAPHelper();
        }
        return ourInstance;
    }

    private Properties props = new Properties();
    private String user;
    private String password;
    private String mailbox;

    private IMAPHelper() {
        Properties config = new Properties();
        try {
            config.load(new FileInputStream(new File("config.properties")));
            props.setProperty("mail.transport.protocol", "imap");
            if(config.getProperty("imapssl").equals("true")) {
                MailSSLSocketFactory socketFactory= new MailSSLSocketFactory();
                socketFactory.setTrustAllHosts(true);
                props.put("mail.imap.starttls.enable", "true");
                props.setProperty("mail.imap.ssl.enable", "true");
                props.put("mail.imap.socketFactory.port", config.getProperty("imapport"));
                props.put("mail.imaps.ssl.socketFactory", socketFactory);
//                props.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            }
            props.setProperty("mail.imap.host", config.getProperty("imaphost"));
            props.setProperty("mail.imap.port", config.getProperty("imapport"));
            props.setProperty("mail.debug", config.getProperty("debug"));
            user = config.getProperty("user");
            password = config.getProperty("password");
            mailbox = config.getProperty("mailbox");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private Thread imapThread;
    private IMAPFolder folder;

    public class StatusObservable extends Observable {
        void notify(String status) {
            setChanged();
            notifyObservers(status);
        }
    }

    public StatusObservable connectionStatus = new StatusObservable();

    /**
     * Start listening to new emails.
     */
    public void startListening() {
        imapThread = new Thread(() -> {
            Session session;
            Store store = null;
            Thread keepalive = null;
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    connectionStatus.notify("connecting");
                    session = Session.getInstance(props);
                    store = session.getStore("imap");
                    store.connect(user, password);
                    folder = (IMAPFolder) store.getFolder(mailbox);
                    folder.open(Folder.READ_WRITE);
                    folder.addMessageCountListener(new MessageCountListener() {
                        @Override
                        public void messagesAdded(MessageCountEvent e) {
                            for (Message m : e.getMessages()) {
                                try {
                                    if (!m.getSubject().equals("IM Message")) {
                                        // Not IM message
                                        m.setFlag(Flags.Flag.SEEN, false);
                                        continue;
                                    }
                                    Email em = parseMessage(m);
                                    if (em != null) {
                                        setChanged();
                                        notifyObservers(em);
                                    }
                                } catch (Exception ee) {
                                    ee.printStackTrace();
                                }
                            }
                        }
                        @Override
                        public void messagesRemoved(MessageCountEvent e) {
                        }
                    });
                    keepalive = new Thread(new KeepAliveRunnable(folder));
                    keepalive.start();
                    log.info("Started listening to new messages");
                    connectionStatus.notify("connected");
                    while(!Thread.interrupted()) {
                        // Keep connection alive
                        folder.idle();
                    }
                    connectionStatus.notify("disconnected");
                    // Stop listening
                    if (keepalive.isAlive()) {
                        keepalive.interrupt();
                    }
                    store.close();
                    return;
                } catch (Exception e) {
                    try {
//                        e.printStackTrace();
                        connectionStatus.notify("lost");
                        log.warning("Connection lost, retry after 10 seconds");
                        Thread.sleep(10000);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    }
                } finally {
                    try {
                        if(store != null) {
                            store.close();
                        }
                        if(keepalive != null) {
                            keepalive.interrupt();
                        }
                    } catch (MessagingException ignored) {
                    }
                }
            }
        });
        imapThread.start();
    }

    /**
     * Stop listening to new emails.
     */
    public void stopListening() {
        imapThread.interrupt();
        try {
            if(folder != null) {
                folder.close();
            }
        } catch (MessagingException ignored) {
        }
    }

    public ArrayList<Email> getMails() throws MessagingException {
        Session session = Session.getDefaultInstance(props);
        Store store = session.getStore("imap");
        store.connect(user, password);
        // Get mails
        IMAPFolder folder = (IMAPFolder) store.getFolder(mailbox);
        folder.open(Folder.READ_WRITE);
        Message[] messages = folder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

        // Sort messages from oldest to recent
        Arrays.sort(messages, (m1, m2) -> {
            try {
                return m1.getSentDate().compareTo(m2.getSentDate());
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        });
        ArrayList<Email> emails = new ArrayList<>();
        // Set messages as read
        for(Message m : messages) {
            if(!m.getSubject().equals("IM Message")) {
                // Not IM message
                m.setFlag(Flags.Flag.SEEN, false);
                continue;
            }
            m.setFlag(Flags.Flag.SEEN, true);
            Email e = parseMessage(m);
            if (e != null) {
                emails.add(e);
            }
        }
        return emails;
    }

    private static Email parseMessage(Message m) {
        Email e = new Email();
        try {
            e.from = m.getFrom()[0].toString();
            for(Address recipient : m.getAllRecipients()) {
                e.to.add(recipient.toString());
            }
            e.subject = m.getSubject();
            e.content = (String)m.getContent();
            e.timestamp = m.getSentDate();
        } catch (Exception e1) {
            // Invalid message
            e1.printStackTrace();
            return null;
        }
        return e;
    }
}

// Reference: https://stackoverflow.com/questions/4155412/javamail-keeping-imapfolder-idle-alive
class KeepAliveRunnable implements Runnable {

    private static final long TIME_INTERVAL = 300000;

    private IMAPFolder folder;

    KeepAliveRunnable(IMAPFolder folder) {
        this.folder = folder;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                Thread.sleep(TIME_INTERVAL);
                // Perform a NOOP just to keep alive the connection
                folder.doCommand(protocol -> {
                    protocol.simpleCommand("NOOP", null);
                    return null;
                });
            } catch (InterruptedException e) {
                return;
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
    }
}
