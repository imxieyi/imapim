package imapim.data;

import com.thoughtworks.xstream.XStream;

import java.util.ArrayList;
import java.util.List;

public class Settings {

    private static Settings ourInstance;

    private XStream xStream;

    public static synchronized Settings getInstance() {
        if(ourInstance == null) {
            ourInstance = new Settings();
        }
        return ourInstance;
    }

    private Settings() {
        xStream = new XStream();
    }

    // Email
    public String smtphost;
    public String smtpport;
    public String imaphost;
    public String imapport;
    public String email;
    public String user;
    public String password;
    public String mailbox;
    public boolean debug;
    public RecipientList recipients;

    //GPG
    public String pubkey;
    public String pubid;
    public String prikey;
    public String priid;
    public String prikeypass;

}

class RecipientList {
    private List<String> list;
    public RecipientList() {
        list = new ArrayList<>();
    }
    public void setList(List<String> list) {
        this.list = list;
    }
    public List<String> getList() {
        return list;
    }
}
