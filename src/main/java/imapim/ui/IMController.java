package imapim.ui;

import imapim.data.Message;
import imapim.protocol.MessageHelper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import org.bouncycastle.openpgp.PGPException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;

import javax.mail.MessagingException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class IMController {

    private static Logger log = Logger.getLogger(IMController.class.getName());

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private WebView messages;

    @FXML
    private HTMLEditor input;

    private Document messagesDocument;

    private String email;
    private String recipient;

    @FXML
    void initialize() {
        input.setHtmlText("");
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream(new File("config.properties")));
            email = prop.getProperty("email");
            recipient = prop.getProperty("recipient");
        } catch (IOException e) {
            log.severe("Failed to load config");
            System.exit(-1);
        }
        messagesDocument = Document.createShell("/");
        Node autoScrollScript = new DataNode(
                "<script language=\"javascript\" tyle=\"text/javascript\">" +
                    "function toBottom() {" +
                        "window.scrollTo(0, document.body.scrollHeight);" +
                    "}" +
                "</script>"
        );
        messagesDocument.head().appendChild(autoScrollScript);
        messagesDocument.body().attr("onload", "toBottom()");
        // Start listening to messages
        MessageHelper.getInstance().addObserver((o, arg) -> {
            Message m = (Message) arg;
            log.fine("Got new message: " + m.content);
            appendMessage(m, false);
        });
        MessageHelper.getInstance().start();
    }

    private void appendMessage(Message m, boolean sent) {
        Node mNode = new DataNode(m.content);
        String color = "blue";
        if (sent) color = "green";
        // Timestamp & Sender
        Node tNode = new DataNode(
                "<p><font face=\"Arial\" color=\"" + color + "\" size=\"2\"><b>" +
                m.from + "  " +
                m.timestamp.toString() +
                "</b></font></p>"
        );
        messagesDocument.body().appendChild(tNode);
        messagesDocument.body().appendChild(mNode);
        Platform.runLater(() -> messages.getEngine().loadContent(messagesDocument.toString()));
    }

    @FXML
    private void send() {
        Document doc = Jsoup.parse(input.getHtmlText());
        log.fine("Html: " + doc.body().html());
        Message m = new Message();
        m.from = email;
        m.to = new ArrayList<>();
        m.to.add(recipient);
        m.timestamp = new Date();
        m.content = doc.body().html();
        try {
            MessageHelper.getInstance().send(m);
            appendMessage(m, true);
            input.setHtmlText("");
        } catch (IOException | PGPException e) {
            log.warning("Failed to encrypt message: " + e.getMessage());
        } catch (MessagingException e) {
            log.warning("Failed to send message: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
