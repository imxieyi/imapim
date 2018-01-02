package imapim.test.security;

import imapim.security.PGPEncrypt;
import org.bouncycastle.openpgp.PGPException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class PGPEncryptTest {

    private static PGPEncrypt encrypt = new PGPEncrypt();

    @BeforeAll
    static void loadPublicKeyFromKeyRing() throws IOException, PGPException {
        encrypt.loadPublicKey("pubtest.txt", "72B3E974");
    }

    @Test
    void testEncryptArmored() throws IOException, PGPException {
        String data = "test";
        byte[] encrypted = encrypt.encrypt(data.getBytes(), true, false);
        System.out.println(new String(encrypted));
    }

    @Test
    void testEncryptNotArmored() throws IOException, PGPException {
        String data = "test";
        byte[] encrypted = encrypt.encrypt(data.getBytes(), false, false);
        System.out.println(new String(encrypted));
    }

}
