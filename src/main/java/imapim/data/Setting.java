package imapim.data;


import imapim.security.AESHelper;
import imapim.ui.contact.ContactController;
import org.json.JSONObject;

import java.io.*;
import java.util.Objects;

public class Setting {

    public static JSONObject instance = null;

    public static JSONObject loadConfig() {
        File f = new File("config.dat");
        if(f.exists()){
            try{
                InputStream is = new FileInputStream(f);
                byte[] bytes = new byte[is.available()];
                if (is.read(bytes)>= 0) {
                    byte[] decrypted = AESHelper.decrypt(bytes, ContactController.getPassword());
                    if (decrypted == null) {
                        throw new IllegalArgumentException("Wrong password");
                    }
                    String jsonTxt = new String(decrypted, "UTF-8");
                    return new JSONObject(jsonTxt);
                }
                is.close();
                // System.out.println(jsonTxt);
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }

    public static boolean saveConfig(JSONObject json){
        File f = new File("config.dat");
        try {
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(Objects.requireNonNull(AESHelper.encrypt(json.toString(), ContactController.getPassword())));
            fos.close();
            instance = json;
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
