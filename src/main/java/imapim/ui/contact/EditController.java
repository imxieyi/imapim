package imapim.ui.contact;

import imapim.data.Person;
import imapim.ui.StageController;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

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
        if(person != null) {
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
        return null;
    }

    @FXML
    private void save() {
        if(validate() == null) {
            person = new Person();
            person.name = name.getText();
            person.email = email.getText();
            person.pubkey = pubkey.getText();
            person.keyid = keyid.getText();
            stage.close();
        }
    }

    @FXML
    private void close() {
        stage.close();
    }

}
