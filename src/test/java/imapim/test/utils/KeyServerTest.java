package imapim.test.utils;


import imapim.data.PubGPGKey;
import imapim.utils.KeyServer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;

class KeyServerTest {
    static  KeyServer server;
    @BeforeAll
    static void initKeyServer(){
        server = KeyServer.getInstance();
    }
    @Test
    void testSearchKeys() {
        // KeyServer server = KeyServer.getInstance();
        ArrayList<PubGPGKey> result = server.searchForKey("fangyd1997");
        System.out.println(result);
    }

    @Test
    void testRegxDate() {
        // KeyServer server = KeyServer.getInstance();
        server.matchDate("pub  1024D/<a href=\"/pks/lookup?op=get&amp;search=0x65FE0B9C5DB89B47\">5DB89B47</a> 2008-04-15 <a href=\"/pks/lookup?op=vindex&amp;search=0x65FE0B9C5DB89B47\">0501065 &lt;fangy_45@163.com&gt;</a>\n");
    }
    @Test
    void testGetKeyContent() {
        // KeyServer server = KeyServer.getInstance();
        server.getKeyContent("65FE0B9C5DB89B47");
    }

    @Test
    void testKeyUpload() {
        boolean result = server.uploadKey("-----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
                "\n" +
                "mQENBFpLNrUBCAC3X5BHPiqjD6n9n0jQhiXrw+QRvTRcASTMiKUKPY15MMFHaly6\n" +
                "+OH0fPt6kIhuo03jRl8DBQuMam6R/Hs8KC7x1/q/fX/iMAo6ni6ZXpugMRFWFsXi\n" +
                "yNKdwceK7/HeNF/aynwS+KOtHne9F3XEeMpDIqMvW9wcDQyVQ54prMTzlyFqSvcs\n" +
                "T/q3sf+iRH9daY/128/wxgOCYcxDQTFxfm61Xc6NG8+hyBaMHEJIKGiF8ze3F1MK\n" +
                "yCN3UvfpA83yTw6y01mKBDEXD0XoRRqUf7v8sg/8Mwgq6qeXKrfNU6YL17z1Ycnf\n" +
                "2KXUDQZl31UPqd9zN8Df3Q4SA4rCMPOEMZIpABEBAAG0H1hpZXlpICh0ZXN0KSA8\n" +
                "dGVzdDEyMzRAc3NzLmNvbT6JATgEEwECACIFAlpLNrUCGwMGCwkIBwMCBhUIAgkK\n" +
                "CwQWAgMBAh4BAheAAAoJEJTppdunl6NY6ZkH/3MJr5uBR7vJPkyttu7rwb9xQNi9\n" +
                "fVVjbw8caFzuxNSu7DdiLVk24A2cXfxL7T7I3jxlu3ARxNwWPFvL3n5CBg3mNUVM\n" +
                "7RaFhfnN4ogXuRQMm5LWbj1J4zhlSakOsDTikmQq6rADDYlaS2bgipniKqZiGhaA\n" +
                "wAzuNZo+m/Ww7aLgWXLLnw2Bxd26QeWVEHxfj1NzdNdOrnzXu38GvQMFrzzztn+y\n" +
                "ppVPkQXiMM9mb1SwoLj4fNlOS8ywGjoe0tOlFU38/JPI5NTryKMQ6vmUbHc3Jbta\n" +
                "/IOBEwgGLRypPEM4M0GU9g5b5hH/zXCDvThQ0JEs0HjGXs+MRNe8EcxArVy5AQ0E\n" +
                "Wks2tQEIAOAREAvU2vmY8Kwra4iI+gp12XF3KC8Coy9K39zUvTtLzNzt4FTZVG6y\n" +
                "jiRrCbXYARsp9beq92hvVveiWuWmvbWn53V43NESGjBqgKCyzpJkC4oyc6z5kn04\n" +
                "vVTGYSFyim3KT0QxegC1taFLM3fJ6l2NYHXdfIobXru9tlwxMmkyoWC8k0PzFJFi\n" +
                "DHCybe/3Ixxl+DB+e+hUwAFRTWstgSfQwIWa4iXH+l/zh2/0oeqRXVbicy9EXfcp\n" +
                "8eWAZYKkirf8Yd5q1lG0f5m9+aF0qxVu1PAP/0kBu/FRfR2C3iVYPdrdZGvyt8wG\n" +
                "DzMwN6/5+xoZA+nPM/E8Q7hIzv9NuLMAEQEAAYkBHwQYAQIACQUCWks2tQIbDAAK\n" +
                "CRCU6aXbp5ejWBnmCACU5ajxe4H64c9CPrr1tfMnnG7fE597Q9aD50/sc+TaisbQ\n" +
                "bEavYBl29nxqi+9gs+/7Z+x2K/2LaNN5qeKynZ6aQSXD1KAr5Po7pVZlQoYn5ft2\n" +
                "j66Qfvo/DDM8sDNfMT0t4XTwLjMqgDWxU/jTSwYcQu9OQoKDtN+Hedq2V/iykW93\n" +
                "0gbBrD/8zwMcx6yy9Fq0vb37Wbcfjc1ek7l/JIH5iFyh3mu9gQ/dhuHhJYJo74Be\n" +
                "2xVGj9GCFPMHgOnBg1hwvdTAg1WOu8qVT1tlaK3AJaXjlrrk1f9DQveGHm5us7+H\n" +
                "UwtKKzr4gKihYZcDPfnEkkk9oorucinJLCVGnphp\n" +
                "=CJ5v\n" +
                "-----END PGP PUBLIC KEY BLOCK-----\n");
        assert result;
    }


}
