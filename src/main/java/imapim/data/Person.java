package imapim.data;

import org.json.JSONObject;

public class Person {

    public String name;
    public String email;
    public String pubkey;
    public String keyid;

    public static Person fromJSON(JSONObject object) {
        Person person = new Person();
        person.name = object.getString("name");
        person.email = object.getString("email");
        person.pubkey = object.getString("pubkey");
        person.keyid = object.getString("keyid");
        return person;
    }

    public JSONObject toJSON() {
        return new JSONObject(this, new String[]{"name", "email", "pubkey", "keyid"});
    }

}
