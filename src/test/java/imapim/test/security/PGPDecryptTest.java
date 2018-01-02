package imapim.test.security;

import imapim.security.PGPDecrypt;
import imapim.security.PGPEncrypt;
import org.bouncycastle.openpgp.PGPException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class PGPDecryptTest {

    static PGPEncrypt encrypt = new PGPEncrypt();
    static PGPDecrypt decrypt = new PGPDecrypt();

    @BeforeAll
    static void loadKeys() throws IOException, PGPException {
        encrypt.loadPublicKey("src/test/resources/pubring.gpg", "8C0A0ADD");
        decrypt.loadPrivateKey("src/test/resources/secring.gpg", "8C0A0ADD", "111111");
    }

    @Test
    void testDecryptArmoredData() throws IOException, PGPException {
        String data = "ggtest";
        byte[] encrypted = encrypt.encrypt(data.getBytes(), true, false);
        byte[] decrypted = decrypt.decrypt(encrypted);
        Assertions.assertEquals(data, new String(decrypted));
    }

    @Test
    void testDecryptNotArmoredData() throws IOException, PGPException {
        String data = "ggtest";
        byte[] encrypted = encrypt.encrypt(data.getBytes(), false, false);
        byte[] decrypted = decrypt.decrypt(encrypted);
        Assertions.assertEquals(data, new String(decrypted));
    }

    @Test
    void testDecryptIntegrityCheckData() throws IOException, PGPException {
        String data = "ggtest";
        byte[] encrypted = encrypt.encrypt(data.getBytes(), false, true);
        byte[] decrypted = decrypt.decrypt(encrypted);
        Assertions.assertEquals(data, new String(decrypted));
    }

}
