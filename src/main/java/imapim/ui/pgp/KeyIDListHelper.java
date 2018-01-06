package imapim.ui.pgp;

import imapim.security.PGPKeyIDHelper;
import javafx.scene.control.ChoiceDialog;
import org.bouncycastle.openpgp.PGPException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

public class KeyIDListHelper {

    public static String selectID(String key, boolean pubkey) throws IOException, PGPException {
        ArrayList<String> choices;
        if (pubkey) {
            choices = PGPKeyIDHelper.publicKey(key);
        } else {
            choices = PGPKeyIDHelper.privateKey(key);
        }
        if (choices.size() <= 0) {
            return null;
        }
        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.setTitle("Select key ID");
        dialog.setHeaderText("Select key ID");
        dialog.setContentText("Choose a key ID:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            return result.get();
        } else {
            return "";
        }
    }

}
