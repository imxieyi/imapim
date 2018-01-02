package imapim.test.security;

import imapim.data.PubGPGKey;
import imapim.security.KeyServer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

class KeyServerTest {
    @Test
    void testSearchKeys() {
        KeyServer server = KeyServer.getInstance();
        ArrayList<PubGPGKey> result = server.searchForKey("fangyd1997");
        System.out.println(result);
    }

    @Test
    void testRegxDate() {
        KeyServer server = KeyServer.getInstance();
        server.matchDate("pub  1024D/<a href=\"/pks/lookup?op=get&amp;search=0x65FE0B9C5DB89B47\">5DB89B47</a> 2008-04-15 <a href=\"/pks/lookup?op=vindex&amp;search=0x65FE0B9C5DB89B47\">0501065 &lt;fangy_45@163.com&gt;</a>\n");
    }
    @Test
    void testGetKeyContent() {
        KeyServer server = KeyServer.getInstance();
        server.getKeyContent("65FE0B9C5DB89B47");
    }


}
