package imapim.security;

import org.bouncycastle.bcpg.ArmoredInputStream;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyDataDecryptorFactory;
import org.bouncycastle.util.io.Streams;

import java.io.*;
import java.util.Iterator;

public class PGPDecrypt {

    private PGPPrivateKey privateKey;

    public void loadPrivateKey(String path, String id, String password) throws IOException, PGPException {
        PGPSecretKeyRingCollection keyRings = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(new FileInputStream(path)), new BcKeyFingerprintCalculator());
        Iterator<PGPSecretKeyRing> it = keyRings.getKeyRings();
        privateKey = null;
        while(it.hasNext() && privateKey == null) {
            PGPSecretKeyRing keyRing = it.next();
            Iterator<PGPSecretKey> kit = keyRing.getSecretKeys();
            while(kit.hasNext() && privateKey == null) {
                PGPSecretKey k= kit.next();
                if(Long.toHexString(k.getKeyID()).toUpperCase().contains(id.toUpperCase())) {
                    PBESecretKeyDecryptor decryptor = new BcPBESecretKeyDecryptorBuilder((new BcPGPDigestCalculatorProvider())).build(password.toCharArray());
                    privateKey = k.extractPrivateKey(decryptor);
                }
            }
        }
        if(privateKey == null) {
            throw new IllegalArgumentException("No private key found!");
        }
    }

    public byte[] decrypt(byte[] data) throws IOException, PGPException {
        boolean armored = true;
        // Fucking ugly way to detect armored data
        for(int i=0;i<5;i++) {
            if (data[i] != '-') {
                armored = false;
                break;
            }
        }
        InputStream is = new ByteArrayInputStream(data);
        if(armored) {
            is = new ArmoredInputStream(is);
        }
        PGPObjectFactory pgpF = new PGPObjectFactory(is, new BcKeyFingerprintCalculator());
        PGPEncryptedDataList enc;
        Object o = pgpF.nextObject();
        if(o instanceof PGPEncryptedDataList) {
            enc = (PGPEncryptedDataList) o;
        } else {
            enc = (PGPEncryptedDataList) pgpF.nextObject();
        }
        Iterator it = enc.getEncryptedDataObjects();
        PGPPublicKeyEncryptedData pbe = null;
        PGPPublicKeyEncryptedData encrypted = null;
        while(it.hasNext()) {
            pbe = (PGPPublicKeyEncryptedData) it.next();
            if(pbe.getKeyID() == privateKey.getKeyID()) {
                encrypted = pbe;
                break;
            }
        }
        if(encrypted == null) {
            throw new PGPException("Private key " + Long.toHexString(privateKey.getKeyID()) + " cannot decrypt given data");
        }
        InputStream clear = encrypted.getDataStream(new BcPublicKeyDataDecryptorFactory(privateKey));
        PGPObjectFactory plainFact = new PGPObjectFactory(clear, new BcKeyFingerprintCalculator());
        Object message = plainFact.nextObject();
        if(message instanceof PGPCompressedData) {
            PGPCompressedData cData = (PGPCompressedData) message;
            PGPObjectFactory pgpFact = new PGPObjectFactory(cData.getDataStream(), new BcKeyFingerprintCalculator());
            message = pgpFact.nextObject();
        }
        byte[] decrypted;
        if(message instanceof PGPLiteralData) {
            PGPLiteralData ld = (PGPLiteralData) message;
            InputStream unc = ld.getInputStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            Streams.pipeAll(unc, bos);
            decrypted = bos.toByteArray();
            bos.close();
        } else if (message instanceof PGPOnePassSignatureList) {
            throw new PGPException("Encrypted message contains a signed message - not literal data.");
        } else {
            throw new PGPException("Message is not a simple encrypted file - type unknown.");
        }
        if(decrypted == null) {
            throw new PGPException("Failed to decrypt message");
        }
        if(pbe.isIntegrityProtected()) {
            if(!pbe.verify()) {
                throw new PGPException("Message failed integrity check");
            }
        }
        return decrypted;
    }

}
