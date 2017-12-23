package imapim.ui;

import javafx.fxml.FXML;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;

import java.net.URL;
import java.util.ResourceBundle;

public class IMController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private WebView messages;

    @FXML
    private HTMLEditor input;

    Document messagesDocument;

    @FXML
    void initialize() {
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
    }

    int i = 0;

    @FXML
    private void send() {
        i++;
        Node countNode = new DataNode(
                "<h1>Count: " + i + "</h1>"
        );
        messagesDocument.body().appendChild(countNode);
        messages.getEngine().loadContent(messagesDocument.toString());
    }

}
