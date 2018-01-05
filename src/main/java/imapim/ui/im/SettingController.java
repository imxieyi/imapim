package imapim.ui.im;

import imapim.ui.StageController;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;

public class SettingController extends StageController {
    @FXML
    private TextField SMTPServer;
    @FXML
    private TextField SMTPPort;
    @FXML
    private TextField IMAPServer;
    @FXML
    private TextField IMAPPort;
    @FXML
    private TextField emailUser;
    @FXML
    private PasswordField emailPassword;
    @FXML
    private TextField SenderAddress;
    @FXML
    private TextField mailBox;
    @FXML
    private TextField privateKey;
    @FXML
    private PasswordField passPhrase;
    @FXML
    private CheckBox sslSMTP;
    @FXML
    private CheckBox sslIMAP;
    @FXML
    private CheckBox debug;

    @FXML
    void initialize() {
        // load the config file and fill the fields
    }

    @FXML
    private void browsePrivateKey() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Private Key");
        // fileChooser.setInitialFileName("pubring.gpg");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("GPG Keyring", "*.gpg"));
        File file = fileChooser.showOpenDialog(stage);
        if(file != null) {
            privateKey.setText(file.getAbsolutePath());
        }
    }


}
