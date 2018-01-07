package imapim.data;

import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;

import java.util.Observable;

public class Chat extends Observable {

    public String name;
    public String email;
    public Document content;

    public Chat(String name, String email) {
        this.name = name;
        this.email = email;
        content = Document.createShell("/");
        // Auto scroll
        Node autoScrollScript = new DataNode(
                "<script language=\"javascript\" tyle=\"text/javascript\">" +
                        "function toBottom() {" +
                        "window.scrollTo(0, document.body.scrollHeight);" +
                        "}" +
                        "</script>"
        );
        content.head().appendChild(autoScrollScript);
        content.body().attr("onload", "toBottom()");
    }

    public void update() {
        setChanged();
        notifyObservers(content);
    }

}
