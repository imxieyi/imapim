package imapim.ui.contact;

import imapim.data.Person;
import imapim.security.AESHelper;
import imapim.ui.StageController;
import imapim.ui.util.SettingController;
import imapim.ui.pgp.GeneratorController;
import imapim.ui.util.PasswordDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.Iterator;
import java.util.Optional;

public class ContactController extends StageController {

    @FXML
    Label status;
    @FXML
    ListView listView;
    ObservableList<Person> personList = FXCollections.observableArrayList();

    private static final String SALT = ".IbRS8hS.KHO";
    private static String appPassword = null;

    public static String getPassword() {
        return appPassword + SALT;
    }

    @FXML
    private void initialize() {
        listView.setItems(personList);
        listView.setCellFactory(param -> new ListViewCell());
        listView.setOnMouseClicked(click -> {
            if (click.getClickCount() == 2) {
            }
        });
        readList();
    }

    @FXML
    private void newPerson() throws IOException {
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
            saveList();
        }
    }

    @FXML
    private void editPerson() throws IOException {
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
                personList.set(personList.indexOf(listView.getSelectionModel().getSelectedItem()), person);
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
                personList.add(Person.fromJSON((JSONObject) it.next()));
            }
        } catch (FileNotFoundException ignored) {
            saveList();
        } catch (IOException e) {
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
        // System.out.println("gdgggggggggggggggggggggggggggg");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/im/setting.fxml"));
        Scene scene = new Scene(loader.load());
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(false);
        ((SettingController) loader.getController()).setStage(stage);
        stage.setTitle("Setting");
        stage.setScene(scene);
        stage.showAndWait();
    }

}
