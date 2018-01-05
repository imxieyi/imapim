package imapim.data;


import org.json.JSONObject;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;

public class Setting {
    public static JSONObject loadConfig() {
        File f = new File("basicConfig.json");
        if(f.exists()){
            try{
                InputStream is = new FileInputStream("basicConfig.json");
                byte[] bytes = new byte[is.available()];
                if (is.read(bytes)>= 0) {
                    String jsonTxt = new String(bytes, "UTF-8");
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
        try {
            FileWriter file = new FileWriter("basicConfig.json");
            file.write(json.toString());
            file.close();
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
