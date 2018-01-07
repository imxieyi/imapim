package imapim.ui.contact;

import imapim.data.Person;
import imapim.data.Setting;
import imapim.protocol.IMAPHelper;
import imapim.security.AESHelper;
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

import java.io.*;
import java.util.Iterator;
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
    ListView listView;
    ObservableList<Person> personList = FXCollections.observableArrayList();

    private static final String SALT = ".IbRS8hS.KHO";
    private static String appPassword = null;
    private boolean connected = false;

    public static String getPassword() {
        return appPassword + SALT;
    }

    @FXML
    private void initialize() throws IOException {
        listView.setItems(personList);
        listView.setCellFactory(param -> new ListViewCell());
        listView.setOnMouseClicked(click -> {
            if (click.getClickCount() == 2) {
                Person p = (Person) listView.getSelectionModel().getSelectedItem();
                if (p != null) {
                    new IMController(p);
                }
            }
        });
        readList();
        if (Setting.instance == null) {
            setting();
            if (Setting.instance == null) {
                System.exit(0);
            }
        }
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
            ((EditController) loader.getController()).setPerson((Person) listView.getSelectionModel().getSelectedItem());
            stage.setTitle("Edit person");
            stage.setScene(scene);
            stage.showAndWait();
            Person person = ((EditController) loader.getController()).getPerson();
            if (person != null) {
                IMHelper.getInstance().remove((Person) listView.getSelectionModel().getSelectedItem());
                personList.set(personList.indexOf(listView.getSelectionModel().getSelectedItem()), person);
                IMHelper.getInstance().add(person);
                saveList();
            }
        }
    }

    @FXML
    private void deletePerson() {
        personList.remove(listView.getSelectionModel().getSelectedItem());
        saveList();
    }

    private void readList() {
        personList.clear();
        File f = new File("contact.dat");
        if (appPassword == null) {
            PasswordDialog prompt = new PasswordDialog();
            prompt.setTitle("App Password");
            if (f.exists()) {
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
                readList();
                return;
            }
            JSONArray contact = new JSONArray(new String(decrypted, "utf-8"));
            Iterator<Object> it = contact.iterator();
            while (it.hasNext()) {
                Person p = Person.fromJSON((JSONObject) it.next());
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

    private void saveList() {
        JSONArray contact = new JSONArray(personList.stream().map(Person::toJSON).toArray());
        File f = new File("contact.dat");
        try {
            OutputStream os = new FileOutputStream(f);
            byte[] encrypted = AESHelper.encrypt(contact.toString(), getPassword());
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
        } else {
            IMHelper.getInstance().addObserver(this);
            IMHelper.getInstance().start();
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

    Semaphore semaphore = new Semaphore(1);

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
                    personList.add(personList.get(personList.size() - 1));
                    for (int i = personList.size() - 1; i > 0; i--) {
                        personList.set(i, personList.get(i - 1));
                    }
                }
                personList.set(0, p);
                semaphore.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

}
