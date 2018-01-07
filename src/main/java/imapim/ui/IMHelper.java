package imapim.ui;

import imapim.data.Chat;
import imapim.data.ChatMessage;
import imapim.data.Message;
import imapim.data.Person;
import imapim.protocol.IMAPHelper;
import imapim.protocol.MessageHelper;
import imapim.protocol.SMTPHelper;
import imapim.ui.im.IMController;
import org.bouncycastle.openpgp.PGPException;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Node;

import javax.mail.internet.MimeUtility;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

public class IMHelper extends Observable implements Observer {

    private static final Logger log = Logger.getLogger(IMHelper.class.getName());

    private static IMHelper ourInstance = null;

    public static synchronized IMHelper getInstance() {
        if (ourInstance == null) {
            ourInstance = new IMHelper();
        }
        return ourInstance;
    }

    Map<String, Chat> chats = new HashMap<>();
    Map<String, Person> people = new HashMap<>();

    private IMHelper() {
    }

    public void reload() {
        stop();
        SMTPHelper.getInstance().reload();
        IMAPHelper.getInstance().reload();
        start();
    }

    public void add(Person person) throws IOException, PGPException {
        Chat chat = new Chat(person.name, person.email);
        chats.put(chat.email, chat);
        people.put(chat.email, person);
        MessageHelper.getInstance().addKey(person.email, person.pubkey, person.keyid);
        log.info("Person added: " + person.email);
    }

    public void openWindow(IMController controller) {
        log.info("Window opened: " + controller.person.email);
        controller.update(this, chats.get(controller.person.email).content);
        chats.get(controller.person.email).addObserver(controller);
    }

    public void closeWindow(IMController controller) {
        log.info("Window closed: " + controller.person.email);
        chats.get(controller.person.email).deleteObserver(controller);
    }

    public void remove(Person person) {
        chats.remove(person.email);
        log.info("Person removed: " + person.email);
    }

    public void start() {
        // Start listening to messages
        MessageHelper.getInstance().addObserver(this);
        MessageHelper.getInstance().start();
        log.info("started");
    }

    public void stop() {
        MessageHelper.getInstance().stop();
        MessageHelper.getInstance().deleteObserver(this);
        log.info("stopped");
    }

    @Override
    public void update(Observable o, Object arg) {
        ChatMessage cm = (ChatMessage) arg;
        appendMessage(cm.message, cm.sent);
    }

    private void appendMessage(Message m, boolean sent) {
        Node mNode = new DataNode(m.content);
        String color = "blue";
        if (sent) color = "green";
        // Fix sender display
        try {
            m.from = MimeUtility.decodeText(m.from);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        m.from = m.from.replaceAll("<", "&lt;");
        m.from = m.from.replaceAll(">", "&gt;");
        // Timestamp & Sender
        Node tNode = new DataNode(
                "\n<p><font face=\"Arial\" color=\"" + color + "\" size=\"2\"><b>" +
                        m.from + "  " +
                        m.timestamp.toString() +
                        "</b></font></p>\n"
        );
        String addr;
        if (sent) {
            addr = m.to.get(0);
        } else {
            addr = m.from;
        }
        Chat c;
        Person p;
        if (chats.containsKey(addr)) {
            c = chats.get(addr);
            p = people.get(addr);
        } else {
            c = new Chat("", addr);
            p = new Person();
            p.email = addr;
            people.put(addr, p);
            chats.put(addr, c);
        }
        setChanged();
        notifyObservers(p);
        log.info("Message appended to " + p.email);
        c.content.body().appendChild(tNode);
        c.content.body().appendChild(mNode);
        c.update();
    }

}
