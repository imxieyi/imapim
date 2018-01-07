package imapim.data;


import imapim.security.AESHelper;
import imapim.ui.contact.ContactController;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class Setting {
    public static JSONObject loadConfig() {
        File f = new File("config.dat");
        if(f.exists()){
            try{
                InputStream is = new FileInputStream(f);
                byte[] bytes = new byte[is.available()];
                if (is.read(bytes)>= 0) {
                    String jsonTxt = new String(AESHelper.decrypt(bytes, ContactController.getPassword()), "UTF-8");
                    return new JSONObject(jsonTxt);
                }
                is.close();
                // System.out.println(jsonTxt);
            }catch (Exception e){
                return null;
            }
        }
        return null;
    }

    public static boolean saveConfig(JSONObject json){
        File f = new File("config.dat");
        try {
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(AESHelper.encrypt(json.toString(), ContactController.getPassword()));
            fos.close();
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
