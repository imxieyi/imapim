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
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Iterator;

// References:
// http://sloanseaman.com/wordpress/2011/08/11/pgp-encryptiondecryption-in-java/
// https://www.programcreek.com/java-api-examples/index.php?source_dir=oobd-master/clients/skds/base_src/org/spongycastle/openpgp/examples/KeyBasedFileProcessor.java
// https://stackoverflow.com/questions/3939447/how-to-encrypt-a-string-stream-with-bouncycastle-pgp-without-starting-with-a-fil
public class PGPEncrypt {

    public static String getAlgorithm(int algId) {
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

    private PGPPublicKey publicKey;

    public void loadPublicKey(String path, String id) throws IOException, PGPException {
        InputStream is = PGPUtil.getDecoderStream(new FileInputStream(path));
        KeyFingerPrintCalculator calculator = new BcKeyFingerprintCalculator();
        PGPPublicKeyRingCollection keyRings = new PGPPublicKeyRingCollection(is, calculator);
        Iterator<PGPPublicKeyRing> it = keyRings.getKeyRings();
        publicKey = null;
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
        PGPKeyEncryptionMethodGenerator generator = new BcPublicKeyKeyEncryptionMethodGenerator(publicKey).setSecureRandom(new SecureRandom());
        cPk.addMethod(generator);
        byte[] bytes = bOut.toByteArray();
        OutputStream cOut = cPk.open(out, bytes.length);
        cOut.write(bytes);
        cOut.close();
        out.close();
        return encOut.toByteArray();
    }

    // What's wrong with following code??????
//    public byte[] encrypt(byte data[], boolean armor, boolean withIntegrityCheck) throws IOException, PGPException {
//        // Compress data with zip
//        ByteArrayInputStream bis = new ByteArrayInputStream(data);
//        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
//        PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(CompressionAlgorithmTags.ZIP);
//        PGPLiteralDataGenerator lData = new PGPLiteralDataGenerator();
//        OutputStream pOut = lData.open(comData.open(bOut), PGPLiteralData.BINARY, PGPLiteralData.CONSOLE, new Date(), new byte[1024]);
//        byte[] buf = new byte[1024];
//        int len;
//        while((len = bis.read(buf)) > 0) {
//            pOut.write(buf, 0, len);
//        }
//        lData.close();
//        byte[] compressed = bOut.toByteArray();
//        bOut.close();
//        // Prepare output stream
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        OutputStream out = bos;
//        if(armor) {
//            out = new ArmoredOutputStream(bos);
//        }
//        // Encrypt data with PGP public key
//        PGPEncryptedDataGenerator encGen = new PGPEncryptedDataGenerator(
//                new BcPGPDataEncryptorBuilder(PGPEncryptedData.AES_256)
//                .setWithIntegrityPacket(withIntegrityCheck)
//                .setSecureRandom(new SecureRandom())
//        );
//        encGen.addMethod(new BcPublicKeyKeyEncryptionMethodGenerator(publicKey));
//        OutputStream cOut = encGen.open(out, compressed);
//        cOut.write(compressed);
//        byte[] encrypted = bos.toByteArray();
//        cOut.close();
//        if(armor) {
//            out.close();
//        }
//        return encrypted;
//    }

    @Test
    void testLoadPublicKeyFromKeyRing() throws IOException, PGPException {
        loadPublicKey("C:\\msys64\\home\\wez73\\.gnupg\\pubring.gpg", "E6B255D0");
        System.out.println("Public key ID: " + Long.toHexString(publicKey.getKeyID()));
        System.out.println("\tAlgorithm: " + getAlgorithm(publicKey.getAlgorithm()));
        System.out.println("\tStrength: " + publicKey.getBitStrength());
        System.out.println("\tFingerprint: " + new String(Hex.encode(publicKey.getFingerprint())));
    }

    @Test
    void testLoadPublicKeyFromTextFile() throws IOException, PGPException {
        loadPublicKey("C:\\Users\\wez73\\public-key.txt", "E6B255D0");
        System.out.println("Public key ID: " + Long.toHexString(publicKey.getKeyID()));
        System.out.println("\tAlgorithm: " + getAlgorithm(publicKey.getAlgorithm()));
        System.out.println("\tStrength: " + publicKey.getBitStrength());
        System.out.println("\tFingerprint: " + new String(Hex.encode(publicKey.getFingerprint())));
    }

    @Test
    void testEncryptArmored() throws IOException, PGPException {
        loadPublicKey("C:\\Users\\wez73\\public-key.txt", "E6B255D0");
        String data = "test";
        byte[] encrypted = encrypt(data.getBytes(), true, false);
        System.out.println(new String(encrypted));
    }

    @Test
    void testEncryptNotArmored() throws IOException, PGPException {
        loadPublicKey("C:\\Users\\wez73\\public-key.txt", "E6B255D0");
        String data = "test";
        byte[] encrypted = encrypt(data.getBytes(), false, false);
        System.out.println(new String(encrypted));
    }

}
