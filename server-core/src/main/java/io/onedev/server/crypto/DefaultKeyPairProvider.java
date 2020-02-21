package io.onedev.server.crypto;

import java.io.StringReader;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.common.util.security.SecurityUtils;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Setting;
import io.onedev.server.model.Setting.Key;
import io.onedev.server.model.support.administration.SshSettings;

@Singleton
public class DefaultKeyPairProvider implements KeyPairProvider {

    private final SettingManager settingManager;

    @Inject
    public DefaultKeyPairProvider(SettingManager settingManager) {
        this.settingManager = settingManager;
    }
    
    @Override
    public Iterable<KeyPair> loadKeys() {
        Setting setting = settingManager.getSetting(Key.SSH);
        SshSettings sshSettings = (SshSettings) setting.getValue();
        
        try (PemReader pemReaderPrivate = new PemReader(new StringReader(sshSettings.getPrivateKey()));
             PemReader pemReaderPublic = new PemReader(new StringReader(sshSettings.getPublicKey()));
                ) {
            KeyFactory kf = SecurityUtils.getKeyFactory(KeyUtils.RSA_ALGORITHM);
            
            PemObject pemObjectPrivate = pemReaderPrivate.readPemObject();
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(pemObjectPrivate.getContent());
            PrivateKey privateKey = kf.generatePrivate(spec);
            
            PemObject pemObjectPublic = pemReaderPublic.readPemObject();
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(pemObjectPublic.getContent());
            PublicKey publicKey = kf.generatePublic(x509EncodedKeySpec);
            
            List<KeyPair> keys = new ArrayList<>();
            
            keys.add(new KeyPair(publicKey, privateKey));
            
            return keys;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
