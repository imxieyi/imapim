package imapim.ui.im;

import imapim.data.Message;
import imapim.data.Person;
import imapim.data.Setting;
import imapim.protocol.MessageHelper;
import imapim.ui.IMHelper;
import imapim.ui.StageController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.*;
import java.util.logging.Logger;

public class IMController extends StageController implements Observer {

    private static Logger log = Logger.getLogger(IMController.class.getName());

    @FXML
    private WebView messages;
    @FXML
    private HTMLEditor input;
    private Document doc = Document.createShell("/");
    public Person person;

    public IMController(Person p) {
        person = p;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/im/im.fxml"));
        Scene scene = null;
        try {
            loader.setController(this);
            scene = new Scene(loader.load());
            stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initStyle(StageStyle.DECORATED);
            stage.setTitle(String.format("%s <%s>", p.name, p.email));
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void initialize() {
        input.setHtmlText("");
        Platform.runLater(() -> stage.setOnCloseRequest((e) -> IMHelper.getInstance().closeWindow(this)));
        IMHelper.getInstance().openWindow(this);
    }

    @FXML
    private void send() {
        Document doc = Jsoup.parse(input.getHtmlText());
        log.fine("Html: " + doc.body().html());
        Message m = new Message();
        m.from = Setting.instance.getString("email");
        m.to = new ArrayList<>();
        m.to.add(person.email);
        m.timestamp = new Date();
        m.content = doc.body().html();
        try {
            MessageHelper.getInstance().send(m);
            input.setHtmlText("");
        } catch (NullPointerException e) {
            log.severe("No public found for " + person.email);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error");
            alert.setContentText("Please add public key to this person!");
            alert.show();
        } catch (IOException | PGPException e) {
            log.severe("Failed to encrypt message: " + e.getMessage());
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
                Document tmp = doc.clone();
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
    private void exit() {
        stage.close();
        IMHelper.getInstance().closeWindow(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        doc = (Document) arg;
        Platform.runLater(() -> messages.getEngine().loadContent(doc.toString()));
    }

}
