package imapim.test.security;

import imapim.security.PGPGenerator;
import org.junit.jupiter.api.Test;

public class PGPGeneratorTest {

    static PGPGenerator generator = new PGPGenerator();

    @Test
    void testGenerate() throws Exception {
        String id = generator.generate("pubring.gpg", "secring.gpg", "gg (gg) <gg@gg.gg>", "111111", true);
        System.out.println("Generated encryption key id: " + id);
    }

}
