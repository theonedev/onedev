package io.onedev.server.git.ssh;

import java.io.IOException;
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.digest.BaseDigest;
import org.apache.sshd.common.util.security.SecurityUtils;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

public class SshKeyUtils {
    public static final BaseDigest MD5_DIGESTER = new BaseDigest("MD5", 512);
    
    public static PublicKey decodePublicKey(String publicKey) throws IOException, GeneralSecurityException {
        try (PemReader pemReaderPublic = new PemReader(new StringReader(publicKey))) {
            KeyFactory kf = SecurityUtils.getKeyFactory(KeyUtils.RSA_ALGORITHM);
            
            PemObject pemObjectPublic = pemReaderPublic.readPemObject();
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(pemObjectPublic.getContent());
            return kf.generatePublic(x509EncodedKeySpec);
        }
    }
    
    public static PrivateKey decodePrivateKey(String privateKey) throws IOException, GeneralSecurityException {
        try (PemReader pemReaderPrivate = new PemReader(new StringReader(privateKey))) {
            KeyFactory kf = SecurityUtils.getKeyFactory(KeyUtils.RSA_ALGORITHM);
            
            PemObject pemObjectPrivate = pemReaderPrivate.readPemObject();
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(pemObjectPrivate.getContent());
            return kf.generatePrivate(spec);
        }
    }
    
}
