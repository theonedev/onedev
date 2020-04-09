package io.onedev.server.git.ssh;

import java.io.IOException;
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;

import org.apache.sshd.common.config.keys.AuthorizedKeyEntry;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.config.keys.PublicKeyEntryResolver;
import org.apache.sshd.common.digest.BaseDigest;
import org.apache.sshd.common.util.security.SecurityUtils;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

public class SshKeyUtils {
    public static final BaseDigest MD5_DIGESTER = new BaseDigest("MD5", 512);
    
    public static PublicKey decodeSshPublicKey(String publicKey) throws IOException, GeneralSecurityException {
        StringReader stringReader = new StringReader(publicKey);
        List<AuthorizedKeyEntry> entries = AuthorizedKeyEntry.readAuthorizedKeys(stringReader, true);

        AuthorizedKeyEntry entry = entries.get(0);
        PublicKey pubEntry = entry.resolvePublicKey(PublicKeyEntryResolver.FAILING);
        return pubEntry;
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
    
}
