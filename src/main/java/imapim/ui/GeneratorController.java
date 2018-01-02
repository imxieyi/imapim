package imapim.ui;

import imapim.security.PGPGenerator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;
import java.util.logging.Logger;

public class GeneratorController {

    private static Logger log = Logger.getLogger(GeneratorController.class.getName());

    @FXML
    private TextField pubpath;
    @FXML
    private TextField pripath;
    @FXML
    private TextField name;
    @FXML
    private TextField email;
    @FXML
    private TextField comment;
    @FXML
    private PasswordField passphrase;
    @FXML
    private ChoiceBox<Integer> strength;

    private Stage stage;

    @FXML
    void initialize() {
        strength.setItems(FXCollections.observableArrayList(1024, 2048, 4096, 8192));
        strength.setValue(2048);
    }

    @FXML
    private void browsePubkey() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Public Keyring");
        fileChooser.setInitialFileName("pubring.gpg");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("GPG Keyring", "*.gpg"));
        File file = fileChooser.showSaveDialog(stage);
        if(file != null) {
            pubpath.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void browsePrikey() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Private Keyring");
        fileChooser.setInitialFileName("secring.gpg");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("GPG Keyring", "*.gpg"));
        File file = fileChooser.showSaveDialog(stage);
        if(file != null) {
            pripath.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void close() {
        stage.close();
    }

    @FXML
    private void generate() {
        String id = "";
        if(name.getText().trim().length() == 0 && email.getText().trim().length() == 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Please input name or email!");
            alert.show();
            return;
        }
        if(name.getText().trim().length() > 0) {
            id += name.getText().trim();
        }
        if(comment.getText().trim().length() > 0) {
            id += " (" + comment.getText().trim() + ")";
        }
        if(email.getText().length() > 0) {
            id += " <" + email.getText().trim() + ">";
        }
        id = id.trim();
        try {
            PGPGenerator.strength = strength.getValue();
            String keyid = PGPGenerator.generate(pubpath.getText(), pripath.getText(), id, passphrase.getText(), true);
            log.info("Key generated: " + keyid);
            ButtonType copy = new ButtonType("Copy Key ID", ButtonBar.ButtonData.APPLY);
            ButtonType close = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "", close, copy);
            alert.setTitle("Success");
            alert.setHeaderText("Success");
            alert.setContentText("User ID: " + id + "\n" +
                                 "Key ID: "  + keyid
            );
            Optional<ButtonType> r = alert.showAndWait();
            if(r.isPresent() && r.get() == copy) {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(keyid);
                clipboard.setContent(content);
            }
        } catch (Exception e) {
            log.severe("Failed to generate key: " + e.getLocalizedMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to generate key!");
            alert.setContentText(e.getLocalizedMessage());
            alert.show();
        } finally {
            stage.close();
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
