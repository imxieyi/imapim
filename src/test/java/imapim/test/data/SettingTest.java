package imapim.test.data;

import imapim.data.Setting;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

public class SettingTest {
    @Test
    void readConfigTest(){
        JSONObject json = Setting.loadConfig();
        if (json != null){
            System.out.println(json.toString());
        }
    }
}
