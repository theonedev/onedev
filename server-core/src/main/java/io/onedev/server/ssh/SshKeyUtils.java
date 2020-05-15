package io.onedev.server.ssh;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;

import org.apache.sshd.common.config.keys.AuthorizedKeyEntry;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.config.keys.PublicKeyEntryResolver;
import org.apache.sshd.common.util.security.SecurityUtils;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;

public class SshKeyUtils {
	
    public static PublicKey decodeSshPublicKey(String publicKey) throws IOException, GeneralSecurityException {
        StringReader stringReader = new StringReader(publicKey);
        List<AuthorizedKeyEntry> entries = AuthorizedKeyEntry.readAuthorizedKeys(stringReader, true);

        AuthorizedKeyEntry entry = entries.get(0);
        return entry.resolvePublicKey(null, PublicKeyEntryResolver.FAILING);
    }
    
    public static PublicKey decodePEMPublicKey(String publicKey) throws IOException, GeneralSecurityException {
        try (PemReader pemReaderPublic = new PemReader(new StringReader(publicKey))) {
            KeyFactory kf = SecurityUtils.getKeyFactory(KeyUtils.RSA_ALGORITHM);
            
            PemObject pemObjectPublic = pemReaderPublic.readPemObject();
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(pemObjectPublic.getContent());
            return kf.generatePublic(x509EncodedKeySpec);
        }
    }
    
    public static PrivateKey decodePEMPrivateKey(String privateKey) throws IOException, GeneralSecurityException {
        try (PemReader pemReaderPrivate = new PemReader(new StringReader(privateKey))) {
            KeyFactory kf = SecurityUtils.getKeyFactory(KeyUtils.RSA_ALGORITHM);
            
            PemObject pemObjectPrivate = pemReaderPrivate.readPemObject();
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(pemObjectPrivate.getContent());
            return kf.generatePrivate(spec);
        }
    }
    
    public static String generatePEMPrivateKey() {
        try (StringWriter privateWriter = new StringWriter();
                PemWriter privatePemWriter = new PemWriter(privateWriter)) {
           KeyPair keyPair = KeyUtils.generateKeyPair("ssh-rsa", 4096);
           
           privatePemWriter.writeObject(new PemObject("RSA PRIVATE KEY", keyPair.getPrivate().getEncoded()));
           privatePemWriter.flush();
           
           return privateWriter.toString();
       } catch (GeneralSecurityException | IOException e) {
           throw new RuntimeException(e);
       }
    }
}
