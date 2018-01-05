package imapim.ui.im;

import imapim.data.Message;
import imapim.protocol.IMAPHelper;
import imapim.protocol.MessageHelper;
import imapim.protocol.SendMailQueue;
import imapim.ui.pgp.GeneratorController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.bouncycastle.openpgp.PGPException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;

import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;
import java.io.*;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;

public class IMController {

    private static Logger log = Logger.getLogger(IMController.class.getName());

    @FXML
    private WebView messages;
    @FXML
    private HTMLEditor input;
    @FXML
    private Label statusLabel;
    @FXML
    private MenuItem connect;

    private Document messagesDocument;
    private String email;
    private String recipient;
    private Stage stage;

    private boolean connected = false;

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
        // Auto scroll
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
            stage.toFront();
        });
        IMAPHelper.getInstance().connectionStatus.addObserver((o, arg) -> {
            String status = (String) arg;
            String stat = "";
            connected = true;
            switch (status) {
                case "connecting":
                    stat = "Connecting...";
                    break;
                case "connected":
                    stat = "Connected";
                    break;
                case "disconnected":
                    stat = "Disconnected";
                    connected = false;
                    break;
                case "lost":
                    stat = "Connection lost";
                    break;
            }
            if(connected) {
                Platform.runLater(() -> connect.setText("Disconnect"));
            } else {
                Platform.runLater(() -> connect.setText("Connect"));
            }
            String finalStat = stat;
            Platform.runLater(() -> statusLabel.setText(finalStat));
        });
        MessageHelper.getInstance().start();
        Platform.runLater(() -> stage.setOnCloseRequest((e) -> {
            MessageHelper.getInstance().stop();
            if(connected) {
                IMAPHelper.getInstance().stopListening();
            }
        }));
        SendMailQueue.getInstance().setCallback((e, m) -> {
            appendMessage(m, true);
        });
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
        m.from = m.from.replaceAll("<","&lt;");
        m.from = m.from.replaceAll(">","&gt;");
        // Timestamp & Sender
        Node tNode = new DataNode(
                "\n<p><font face=\"Arial\" color=\"" + color + "\" size=\"2\"><b>" +
                m.from + "  " +
                m.timestamp.toString() +
                "</b></font></p>\n"
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
            input.setHtmlText("");
        } catch (IOException | PGPException e) {
            log.warning("Failed to encrypt message: " + e.getMessage());
        } catch (MessagingException e) {
            log.warning("Failed to send message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void openImage() {
        // Show file dialog
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Image");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image File",
                "*.png", "*.jpg", "*.jpe", "*.jpeg", "*.gif", "*.webp"));
        File file = fileChooser.showOpenDialog(stage);
        if(file == null) {
            return;
        }
        // Append image
        Document doc = Jsoup.parse(input.getHtmlText());
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            String mimetype = URLConnection.guessContentTypeFromName(file.getName());
            String encoded = Base64.getEncoder().encodeToString(buffer);
            DataNode imgNode = new DataNode(
                    "<img style='max-width:100% !important;' src='data:" + mimetype + ";base64, " + encoded + "'/>"
            );
            doc.body().appendChild(imgNode);
            input.setHtmlText(doc.html());
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error");
            alert.setContentText("Failed to open file: " + e.getLocalizedMessage() + "\n" + file.getAbsolutePath());
            alert.show();
            log.severe("Failed to open file: " + e.getLocalizedMessage() + "\n" + file.getAbsolutePath());
        }
    }

    @FXML
    private void saveSession() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Session");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML Document", "*.html"));
        File file = fileChooser.showSaveDialog(stage);
        if(file != null) {
            try {
                FileOutputStream fos = new FileOutputStream(file);
                Document tmp = messagesDocument.clone();
                tmp.body().removeAttr("onload");
                tmp.head().childNodes().get(0).remove();
                fos.write(tmp.html().getBytes());
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Error");
                alert.setContentText("Failed to write to file:\n" + file.getAbsolutePath());
                alert.show();
                log.severe("Failed to write to file: " + file.getAbsolutePath());
            }
        }
    }

    @FXML
    private void connectClicked() {
        if(connected) {
            MessageHelper.getInstance().stop();
        } else {
            MessageHelper.getInstance().start();
        }
    }

    @FXML
    private void genKey() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/pgp/generator.fxml"));
        Scene scene = new Scene(loader.load());
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(false);
        ((GeneratorController)loader.getController()).setStage(stage);
        stage.setTitle("PGP Key Pair Generator");
        stage.setScene(scene);
        stage.showAndWait();
    }

    @FXML
    private void exit() {
        stage.close();
        MessageHelper.getInstance().stop();
        if(connected) {
            IMAPHelper.getInstance().stopListening();
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

}
