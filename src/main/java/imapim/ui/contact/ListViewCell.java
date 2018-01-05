package imapim.ui.contact;

import imapim.data.Person;
import javafx.scene.control.ListCell;

public class ListViewCell extends ListCell<Person> {

    @Override
    protected void updateItem(Person item, boolean empty) {
        super.updateItem(item, empty);
        if(item != null) {
            PersonCell cell = new PersonCell(item);
            setGraphic(cell.getBox());
        } else {
            setGraphic(null);
        }
    }
}
