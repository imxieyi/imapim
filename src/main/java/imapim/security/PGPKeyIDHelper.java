package imapim.security;

import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.KeyFingerPrintCalculator;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

public class PGPKeyIDHelper {

    public static ArrayList<String> publicKey(String path) throws IOException, PGPException {
        ArrayList<String> keys = new ArrayList<>();
        InputStream is = PGPUtil.getDecoderStream(new FileInputStream(path));
        KeyFingerPrintCalculator calculator = new BcKeyFingerprintCalculator();
        PGPPublicKeyRingCollection keyRings = new PGPPublicKeyRingCollection(is, calculator);
        Iterator<PGPPublicKeyRing> it = keyRings.getKeyRings();
        while (it.hasNext()) {
            PGPPublicKeyRing keyRing = it.next();
            Iterator<PGPPublicKey> kit = keyRing.getPublicKeys();
            while (kit.hasNext()) {
                PGPPublicKey k = kit.next();
                if (k.isEncryptionKey()) {
                    keys.add(Long.toHexString(k.getKeyID()).toUpperCase());
                }
            }
        }
        return keys;
    }

    public static ArrayList<String> privateKey(String path) throws IOException, PGPException {
        ArrayList<String> keys = new ArrayList<>();
        PGPSecretKeyRingCollection keyRings = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(new FileInputStream(path)), new BcKeyFingerprintCalculator());
        Iterator<PGPSecretKeyRing> it = keyRings.getKeyRings();
        while (it.hasNext()) {
            PGPSecretKeyRing keyRing = it.next();
            Iterator<PGPSecretKey> kit = keyRing.getSecretKeys();
            while (kit.hasNext()) {
                PGPSecretKey k = kit.next();
                if (!k.isPrivateKeyEmpty() && !k.isSigningKey()) {
                    keys.add(Long.toHexString(k.getKeyID()).toUpperCase());
                }
            }
        }
        return keys;
    }

}
