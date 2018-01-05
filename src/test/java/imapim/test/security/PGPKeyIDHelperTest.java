package imapim.test.security;

import imapim.security.PGPKeyIDHelper;
import org.bouncycastle.openpgp.PGPException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;

public class PGPKeyIDHelperTest {

    @Test
    public void testPublicKey() throws IOException, PGPException {
        ArrayList<String> keys = PGPKeyIDHelper.publicKey("src/test/resources/pubring.gpg");
        Assertions.assertNotEquals(0, keys.size());
        System.out.println(keys);
    }

    @Test
    public void testPrivateKey() throws IOException, PGPException {
        ArrayList<String> keys = PGPKeyIDHelper.privateKey("src/test/resources/secring.gpg");
        Assertions.assertNotEquals(0, keys.size());
        System.out.println(keys);
    }

}
