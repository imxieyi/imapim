package imapim.ui.contact;

import imapim.data.Person;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class PersonCell {

    @FXML
    VBox vBox;
    @FXML
    Label name;
    @FXML
    Label key;

    public PersonCell(Person person) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/contact/listitem.fxml"));
        loader.setController(this);
        try {
            loader.load();
            name.setText(String.format("%s <%s>", person.name, person.email));
            key.setText(String.format("%s (%s)", person.pubkey, person.keyid));
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public VBox getBox() {
        return vBox;
    }

}
