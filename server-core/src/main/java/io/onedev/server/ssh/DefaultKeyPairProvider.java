package io.onedev.server.ssh;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.common.session.SessionContext;

import com.google.common.collect.Lists;

import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.SshSetting;

@Singleton
public class DefaultKeyPairProvider implements KeyPairProvider {

    private final SettingManager settingManager;

    @Inject
    public DefaultKeyPairProvider(SettingManager settingManager) {
        this.settingManager = settingManager;
    }
    
    @Override
    public Iterable<KeyPair> loadKeys(SessionContext session) {
        SshSetting sshSetting = settingManager.getSshSetting();
        
        try {
            PrivateKey privateKey = SshKeyUtils.decodePEMPrivateKey(sshSetting.getPemPrivateKey());
            PublicKey publicKey = KeyUtils.recoverPublicKey(privateKey);
            return Lists.newArrayList(new KeyPair(publicKey, privateKey));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
