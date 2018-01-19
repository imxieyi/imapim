package imapim.ui.contact;

import imapim.data.Person;
import imapim.data.Setting;
import imapim.protocol.IMAPHelper;
import imapim.security.AESHelper;
import imapim.security.PGPDecrypt;
import imapim.ui.IMHelper;
import imapim.ui.StageController;
import imapim.ui.im.IMController;
import imapim.ui.pgp.GeneratorController;
import imapim.ui.util.PasswordDialog;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.bouncycastle.openpgp.PGPException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;

import javax.crypto.BadPaddingException;
import java.io.*;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.concurrent.Semaphore;

public class ContactController extends StageController implements Observer {

    @FXML
    Label statusLabel;
    @FXML
    MenuItem connect;
    @FXML
    ListView<Person> listView;
    private ObservableList<Person> personList = FXCollections.observableArrayList();

    private static final String SALT = ".IbRS8hS.KHO";
    private static String appPassword = null;
    private boolean connected = false;

    public static String getPassword() {
        return appPassword + SALT;
    }

    @FXML
    private void initialize() throws IOException {
        inputAppPassword();
        while (Setting.instance == null && (new File("config.dat").exists() || new File("contact.dat").exists())) {
            try {
                Setting.instance = Setting.loadConfig();
            } catch (IllegalArgumentException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("PasswordError");
                alert.setHeaderText("Wrong password!");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
                appPassword = null;
                inputAppPassword();
            }
        }
        if (Setting.instance == null) {
            setting();
            if (Setting.instance == null) {
                System.exit(0);
            }
        } else {
            try {
                new PGPDecrypt().loadPrivateKey(Setting.instance.optString("privatekeyFile"),
                        Setting.instance.optString("privatekeyId"),
                        Setting.instance.optString("privatekeyPass"));
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Private Key Error");
                alert.setHeaderText("Failed to load private key");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
                Setting.instance = null;
                setting();
                if (Setting.instance == null) {
                    System.exit(0);
                }
            }
        }
        listView.setItems(personList);
        listView.setCellFactory(param -> new ListViewCell());
        listView.setOnMouseClicked(click -> {
            if (click.getClickCount() == 2) {
                Person p = listView.getSelectionModel().getSelectedItem();
                if (p != null) {
                    new IMController(p);
                }
            }
        });
        readList();
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
            if (connected) {
                Platform.runLater(() -> connect.setText("Disconnect"));
            } else {
                Platform.runLater(() -> connect.setText("Connect"));
            }
            String finalStat = stat;
            Platform.runLater(() -> statusLabel.setText(finalStat));
        });
        Platform.runLater(() -> stage.setOnCloseRequest(event -> IMHelper.getInstance().stop()));
        if (Setting.instance != null) {
            IMHelper.getInstance().addObserver(this);
            IMHelper.getInstance().start();
        }
    }

    @FXML
    private void newPerson() throws IOException, PGPException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/contact/edit.fxml"));
        Scene scene = new Scene(loader.load());
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(false);
        ((EditController) loader.getController()).setStage(stage);
        stage.setTitle("New person");
        stage.setScene(scene);
        stage.showAndWait();
        Person person = ((EditController) loader.getController()).getPerson();
        if (person != null) {
            personList.add(person);
            IMHelper.getInstance().add(person);
            saveList();
        }
    }

    @FXML
    private void editPerson() throws IOException, PGPException {
        if (listView.getSelectionModel().getSelectedItem() != null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/contact/edit.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UTILITY);
            stage.setResizable(false);
            ((EditController) loader.getController()).setStage(stage);
            ((EditController) loader.getController()).setPerson(listView.getSelectionModel().getSelectedItem());
            stage.setTitle("Edit person");
            stage.setScene(scene);
            stage.showAndWait();
            Person person = ((EditController) loader.getController()).getPerson();
            if (person != null) {
                Document content = IMHelper.getInstance().getChat(listView.getSelectionModel().getSelectedItem()).content;
                IMHelper.getInstance().remove(listView.getSelectionModel().getSelectedItem());
                personList.set(personList.indexOf(listView.getSelectionModel().getSelectedItem()), person);
                IMHelper.getInstance().add(person);
                IMHelper.getInstance().getChat(person).content = content;
                saveList();
            }
        }
    }

    @FXML
    private void deletePerson() {
        personList.remove(listView.getSelectionModel().getSelectedItem());
        saveList();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void readList() {
        personList.clear();
        File f = new File("contact.dat");
        try {
            InputStream is = new FileInputStream(f);
            byte[] b = new byte[is.available()];
            is.read(b);
            is.close();
            byte[] decrypted = AESHelper.decrypt(b, getPassword());
            if (decrypted == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error reading contact");
                alert.setHeaderText("Wrong password!");
                alert.showAndWait();
                appPassword = null;
                inputAppPassword();
                return;
            }
            JSONArray contact = new JSONArray(new String(decrypted, "utf-8"));
            for (Object aContact : contact) {
                Person p = Person.fromJSON((JSONObject) aContact);
                personList.add(p);
                IMHelper.getInstance().add(p);
            }
        } catch (FileNotFoundException ignored) {
            saveList();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("IO Error");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void inputAppPassword() {
        if (appPassword == null) {
            PasswordDialog prompt = new PasswordDialog();
            prompt.setTitle("App Password");
            if (new File("config.dat").exists() || new File("contact.dat").exists()) {
                prompt.setHeaderText("Please input password");
            } else {
                prompt.setHeaderText("Please set password");
            }
            Optional<String> result = prompt.showAndWait();
            if (result.isPresent()) {
                appPassword = result.get();
            } else {
                System.exit(0);
            }
        }
    }

    private void saveList() {
        JSONArray contact = new JSONArray(personList.stream().map(Person::toJSON).toArray());
        File f = new File("contact.dat");
        try {
            OutputStream os = new FileOutputStream(f);
            byte[] encrypted = AESHelper.encrypt(contact.toString(), getPassword());
            assert encrypted != null;
            os.write(encrypted);
            os.close();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("IO Error");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void onConnect() {
        if (connected) {
            IMHelper.getInstance().stop();
            IMHelper.getInstance().deleteObserver(this);
            connect.setText("Connect");
        } else {
            IMHelper.getInstance().addObserver(this);
            IMHelper.getInstance().start();
            connect.setText("Disconnect");
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
        ((GeneratorController) loader.getController()).setStage(stage);
        stage.setTitle("PGP Key Pair Generator");
        stage.setScene(scene);
        stage.showAndWait();
    }

    @FXML
    private void setting() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/im/setting.fxml"));
        Scene scene = new Scene(loader.load());
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(false);
        ((StageController) loader.getController()).setStage(stage);
        stage.setTitle("Setting");
        stage.setScene(scene);
        stage.showAndWait();
    }

    @FXML
    public void onExit() {
        IMHelper.getInstance().stop();
        stage.close();
    }

    private Semaphore semaphore = new Semaphore(1);

    @Override
    public void update(Observable o, Object arg) {
        Platform.runLater(() -> {
            try {
                semaphore.acquire();
                Person p = (Person) arg;
                if (personList.contains(p)) {
                    int index = personList.indexOf(p);
                    for (int i = index; i > 0; i--) {
                        personList.set(i, personList.get(i - 1));
                    }
                } else {
                    if (personList.size() > 0) {
                        personList.add(personList.get(personList.size() - 1));
                    }
                    for (int i = personList.size() - 1; i > 0; i--) {
                        personList.set(i, personList.get(i - 1));
                    }
                }
                if (personList.size() > 0) {
                    personList.set(0, p);
                } else {
                    personList.add(p);
                }
                semaphore.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

}
