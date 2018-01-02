package imapim.security;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.PublicKeyAlgorithmTags;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.KeyFingerPrintCalculator;
import org.bouncycastle.openpgp.operator.PGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.PGPKeyEncryptionMethodGenerator;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.bc.BcPGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyKeyEncryptionMethodGenerator;

import java.io.*;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

// References:
// http://sloanseaman.com/wordpress/2011/08/11/pgp-encryptiondecryption-in-java/
// https://www.programcreek.com/java-api-examples/index.php?source_dir=oobd-master/clients/skds/base_src/org/spongycastle/openpgp/examples/KeyBasedFileProcessor.java
// https://stackoverflow.com/questions/3939447/how-to-encrypt-a-string-stream-with-bouncycastle-pgp-without-starting-with-a-fil
public class PGPEncrypt {

    static String getAlgorithm(int algId) {
        switch (algId) {
            case PublicKeyAlgorithmTags.RSA_GENERAL:
                return "RSA_GENERAL";
            case PublicKeyAlgorithmTags.RSA_ENCRYPT:
                return "RSA_ENCRYPT";
            case PublicKeyAlgorithmTags.RSA_SIGN:
                return "RSA_SIGN";
            case PublicKeyAlgorithmTags.ELGAMAL_ENCRYPT:
                return "ELGAMAL_ENCRYPT";
            case PublicKeyAlgorithmTags.DSA:
                return "DSA";
            case PublicKeyAlgorithmTags.ECDH:
                return "ECDH";
            case PublicKeyAlgorithmTags.ECDSA:
                return "ECDSA";
            case PublicKeyAlgorithmTags.ELGAMAL_GENERAL:
                return "ELGAMAL_GENERAL";
            case PublicKeyAlgorithmTags.DIFFIE_HELLMAN:
                return "DIFFIE_HELLMAN";
        }
        return "unknown";
    }

    private ArrayList<PGPPublicKey> publicKeys = new ArrayList<>();

    public void loadPublicKey(String path, String id) throws IOException, PGPException {
        InputStream is = PGPUtil.getDecoderStream(new FileInputStream(path));
        KeyFingerPrintCalculator calculator = new BcKeyFingerprintCalculator();
        PGPPublicKeyRingCollection keyRings = new PGPPublicKeyRingCollection(is, calculator);
        Iterator<PGPPublicKeyRing> it = keyRings.getKeyRings();
        PGPPublicKey publicKey = null;
        while (publicKey == null && it.hasNext()) {
            PGPPublicKeyRing keyRing = it.next();
            Iterator<PGPPublicKey> kit = keyRing.getPublicKeys();
            while (publicKey == null && kit.hasNext()) {
                PGPPublicKey k = kit.next();
                if (k.isEncryptionKey() && Long.toHexString(k.getKeyID()).toUpperCase().contains(id.toUpperCase())) {
                    publicKey = k;
                }
            }
        }
        if (publicKey == null) {
            throw new IllegalArgumentException("No encryption key in given key ring");
        }
        publicKeys.add(publicKey);
    }

    public byte[] encrypt(byte data[], boolean armor, boolean withIntegrityCheck) throws IOException, PGPException {
        ByteArrayOutputStream encOut = new ByteArrayOutputStream();
        OutputStream out = encOut;
        if(armor) {
            out = new ArmoredOutputStream(out);
        }
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        // Compress data
        PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(PGPCompressedDataGenerator.ZIP);
        OutputStream cos = comData.open(bOut);
        // destination
        PGPLiteralDataGenerator lData = new PGPLiteralDataGenerator();
        OutputStream pOut = lData.open(cos,
                PGPLiteralData.BINARY,
                PGPLiteralData.CONSOLE,
                data.length,
                new Date()
                );
        pOut.write(data);
        lData.close();
        comData.close();
        // Encrypt data
        PGPDataEncryptorBuilder builder = new BcPGPDataEncryptorBuilder(PGPEncryptedData.AES_256)
                .setWithIntegrityPacket(withIntegrityCheck)
                .setSecureRandom(new SecureRandom());
        PGPEncryptedDataGenerator cPk = new PGPEncryptedDataGenerator(builder);
        for(PGPPublicKey publicKey : publicKeys) {
            PGPKeyEncryptionMethodGenerator generator = new BcPublicKeyKeyEncryptionMethodGenerator(publicKey).setSecureRandom(new SecureRandom());
            cPk.addMethod(generator);
        }
        byte[] bytes = bOut.toByteArray();
        OutputStream cOut = cPk.open(out, bytes.length);
        cOut.write(bytes);
        cOut.close();
        out.close();
        return encOut.toByteArray();
    }

}
