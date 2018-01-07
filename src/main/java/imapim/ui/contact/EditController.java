package imapim.ui.contact;

import imapim.data.Email;
import imapim.data.Person;
import imapim.data.PubGPGKey;
import imapim.security.PGPEncrypt;
import imapim.ui.StageController;
import imapim.ui.pgp.KeyIDListHelper;
import imapim.utils.KeyServer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class EditController extends StageController {

    @FXML
    TextField name;
    @FXML
    TextField email;
    @FXML
    TextField pubkey;
    @FXML
    TextField keyid;

    private Person person = null;

    public void setPerson(Person person) {
        if (person != null) {
            name.setText(person.name);
            email.setText(person.email);
            pubkey.setText(person.pubkey);
            keyid.setText(person.keyid);
        }
    }

    public Person getPerson() {
        return person;
    }

    private String validate() {
        if (name.getText().length() <= 0) {
            return "Please input name!";
        }
        if (email.getText().length() <= 0) {
            return "Please input email!";
        }
        if (!Email.validate(email.getText())) {
            return "Invalid email address!";
        }
        if (pubkey.getText().length() <= 0) {
            return "Please input public key path!";
        }
        try {
            PGPEncrypt.getPublicKey(pubkey.getText(), keyid.getText());
        } catch (Exception e) {
            return e.getMessage();
        }
        return null;
    }

    @FXML
    private void browseKey() {
        // Show file dialog
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Browse public key");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            pubkey.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void selectKey() {
        String msg = null;
        if (pubkey.getText().length() <= 0) {
            msg = "Please input public key path!";
        } else {
            try {
                String id = KeyIDListHelper.selectID(pubkey.getText(), true);
                if (id == null) {
                    msg = "No public key found in given file!";
                } else if (!id.equals("")) {
                    keyid.setText(id);
                }
            } catch (Exception e) {
                msg = e.getMessage();
            }
        }
        if (msg != null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText(msg);
            alert.show();
        }
    }

    @FXML
    private void save() {
        String error = validate();
        if (error == null) {
            person = new Person();
            person.name = name.getText();
            person.email = email.getText();
            person.pubkey = pubkey.getText();
            person.keyid = keyid.getText();
            stage.close();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText(error);
            alert.show();
        }
    }

    @FXML
    private void close() {
        stage.close();
    }

    @FXML
    private void searchForPubKey() throws IOException {
        // System.out.println("gdgggggggggggggggggggggggggggg");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/util/SearchPubKey.fxml"));
        Scene scene = new Scene(loader.load());
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(false);
        SearchPubKeyController controller = (SearchPubKeyController) loader.getController();
        controller.setStage(stage);
        stage.setTitle("Search For a Public Key");
        stage.setScene(scene);
        stage.showAndWait();
        PubGPGKey result = SearchPubKeyController.getSelected();
        savePubKey(result);
    }

    private void savePubKey(PubGPGKey result) throws IOException {
        if (result == null) {
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Public Keyring");
        fileChooser.setInitialFileName("pubring.gpg");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("GPG Keyring", "*.gpg"));
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            KeyServer server = KeyServer.getInstance();
            String pubKey = server.getKeyContent(result.getFingerPrint());
            // System.out.println(file.getAbsoluteFile());
            FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.print(pubKey);
            printWriter.close();
            pubkey.setText(file.getAbsolutePath());
        }
    }
}
