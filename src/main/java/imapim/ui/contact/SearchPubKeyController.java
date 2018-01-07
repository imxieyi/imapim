package imapim.ui.contact;

import imapim.data.PubGPGKey;
import imapim.ui.StageController;
import imapim.utils.KeyServer;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.ArrayList;

public class SearchPubKeyController extends StageController {
    @FXML
    private TableView<PubGPGKey> resultTable = new TableView<PubGPGKey>();
    @FXML
    private TableColumn<PubGPGKey, String> primaryKeyId;
    @FXML
    private TableColumn<PubGPGKey, String> keyDetails;
    @FXML
    private TextField keyword;

    private static boolean selected = false;
    private static PubGPGKey selectedItem = null;

    private ObservableList<PubGPGKey> resultList = FXCollections.observableArrayList();

    public static PubGPGKey getSelected() {
        if (selected) {
            return selectedItem;
        } else {
            return null;
        }
    }

    @FXML
    void initialize() {
        selected = false;
        resultList.addAll(new ArrayList<PubGPGKey>());
        resultTable.setItems(resultList);
        resultTable.setRowFactory(tv -> {
            TableRow<PubGPGKey> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    PubGPGKey rowData = row.getItem();
                    System.out.println("Double click on: " + rowData.getFingerPrint());
                    selected = true;
                    selectedItem = rowData;
                    stage.close();
                }
            });
            return row;
        });
        //System.out.println("aaa");
    }

    @FXML
    private void search() {
        resultList.clear();
        // resultList.removeAll();
        // resultTable.setItems(resultList);
        // resultTable.refresh();
        KeyServer server = KeyServer.getInstance();
        ArrayList<PubGPGKey> result = server.searchForKey(keyword.getText());
        resultList.addAll(result);
        primaryKeyId.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getFingerPrint()));
        keyDetails.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getUserID()));
    }
}


