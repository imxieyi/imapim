package imapim.ui.util;

import imapim.data.Setting;
import imapim.ui.StageController;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import org.json.JSONObject;

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
    private TextField senderAddress;
    @FXML
    private TextField mailBox;
    @FXML
    private TextField privateKey;
    @FXML
    private PasswordField passPhrase;
    @FXML
    private TextField privateKeyId;
    @FXML
    private TextField keyServer;
    @FXML
    private CheckBox sslSMTP;
    @FXML
    private CheckBox sslIMAP;
    @FXML
    private CheckBox debug;

    @FXML
    void initialize() {
        // load the config file and fill the fields
        JSONObject setting = Setting.loadConfig();
        if (setting != null) {
            SMTPServer.setText(setting.optString("smtphost"));
            SMTPPort.setText(setting.optString("smtpport"));
            sslSMTP.setSelected(setting.optBoolean("smtpssl", true));
            sslIMAP.setSelected(setting.optBoolean("imapssl", true));
            IMAPServer.setText(setting.optString("imaphost"));
            IMAPPort.setText(setting.optString("imapport"));
            emailUser.setText(setting.optString("user"));
            emailPassword.setText(setting.optString("password"));
            senderAddress.setText(setting.optString("email"));
            mailBox.setText(setting.optString("mailbox"));
            privateKey.setText(setting.optString("privatekeyFile"));
            passPhrase.setText(setting.optString("privatekeyPass"));
            privateKeyId.setText(setting.optString("privatekeyId"));
            debug.setSelected(setting.optBoolean("debug", true));
            keyServer.setText(setting.optString("keyServer"));
        }
    }

    @FXML
    private void browsePrivateKey() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Private Key");
        // fileChooser.setInitialFileName("pubring.gpg");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("GPG Keyring", "*.gpg"));
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            privateKey.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void save() {
        JSONObject json = new JSONObject();
        json.put("smtphost", SMTPServer.getText());
        json.put("smtpport", SMTPPort.getText());
        json.put("smtpssl", sslSMTP.isSelected());
        json.put("imaphost", IMAPServer.getText());
        json.put("imapport", IMAPPort.getText());
        json.put("imapssl", sslIMAP.isSelected());
        json.put("email", senderAddress.getText());
        json.put("user", emailUser.getText());
        json.put("password", emailPassword.getText());
        json.put("mailbox", mailBox.getText());
        json.put("debug", debug.isSelected());
        json.put("privatekeyFile", privateKey.getText());
        json.put("privatekeyPass", passPhrase.getText());
        json.put("privatekeyId", privateKeyId.getText());
        json.put("keyServer", keyServer.getText());
        Setting.saveConfig(json);
        stage.close();
    }

    @FXML
    private void cancel() {
        stage.close();
    }


}
