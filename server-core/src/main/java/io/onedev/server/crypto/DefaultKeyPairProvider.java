package io.onedev.server.crypto;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.git.ssh.SshKeyUtils;
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
        
        try {
            PrivateKey privateKey = SshKeyUtils.decodePEMPrivateKey(sshSettings.getPrivateKey());
            PublicKey publicKey = KeyUtils.recoverPublicKey(privateKey);
            
            List<KeyPair> keys = new ArrayList<>();
            
            keys.add(new KeyPair(publicKey, privateKey));
            
            return keys;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
