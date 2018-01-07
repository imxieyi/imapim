package imapim.ui.util;

import imapim.data.Setting;
import imapim.security.PGPDecrypt;
import imapim.ui.IMHelper;
import imapim.ui.StageController;
import imapim.ui.pgp.GeneratorController;
import imapim.ui.pgp.KeyIDListHelper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

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

    private String validate() {
        if (SMTPServer.getText().length() <= 0) {
            return "Please input SMTP server!";
        }
        if (SMTPPort.getText().length() <= 0) {
            return "Please input SMTP Port!";
        }
        if (IMAPServer.getText().length() <= 0) {
            return "Please input IMAP server!";
        }
        if (IMAPPort.getText().length() <= 0) {
            return "Please input IMAP Port!";
        }
        if (senderAddress.getText().length() <= 0) {
            return "Please input sender email address!";
        }
        if (emailUser.getText().length() <= 0) {
            return "Please input email user name!";
        }
        if (mailBox.getText().length() <= 0) {
            return "Please input mailbox!";
        }
        if (privateKey.getText().length() <= 0) {
            return "Please input private key path!";
        }
        try {
            PGPDecrypt decrypt = new PGPDecrypt();
            decrypt.loadPrivateKey(privateKey.getText(), privateKeyId.getText(), passPhrase.getText());
        } catch (Exception e) {
            return e.getMessage();
        }
        return null;
    }

    @FXML
    private void save() {
        String msg = validate();
        if (msg != null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText(msg);
            alert.show();
            return;
        }
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
        Setting.instance = Setting.loadConfig();
        IMHelper.getInstance().reload();
    }

    @FXML
    private void select() {
        String msg = null;
        if (privateKey.getText().length() <= 0) {
            msg = "Please input private key path!";
        } else {
            try {
                String id = KeyIDListHelper.selectID(privateKey.getText(), false);
                if (id == null) {
                    msg = "No private key found in given file!";
                } else if (!id.equals("")) {
                    privateKeyId.setText(id);
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
    private void generator() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/pgp/generator.fxml"));
        Scene scene = new Scene(loader.load());
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(false);
        ((GeneratorController) loader.getController()).setStage(stage);
        stage.setTitle("PGP Key Pair Generator");
        stage.setScene(scene);
        stage.showAndWait();
    }

    @FXML
    private void cancel() {
        stage.close();
    }


}
