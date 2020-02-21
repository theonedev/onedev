package io.onedev.server.crypto;

import static org.junit.Assert.assertEquals;
import java.io.StringWriter;
import java.security.KeyPair;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.junit.Test;
import org.mockito.Mockito;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Setting;
import io.onedev.server.model.Setting.Key;
import io.onedev.server.model.support.administration.SshSettings;

public class DefaultKeyPairProviderGeneratorTest {
    @Test
    public void generateAndDecode() throws Exception {
        DefaultServerKeyPairPopulator populator = new DefaultServerKeyPairPopulator();
        SshSettings sshSettings = new SshSettings();
        
        populator.populateSettings(sshSettings);
        
        SettingManager settingManager = Mockito.mock(SettingManager.class);
        Setting setting = new Setting();
        setting.setValue(sshSettings);
        
        Mockito.when(settingManager.getSetting(Mockito.any(Key.class))).thenReturn(setting);
        DefaultKeyPairProvider provider = new DefaultKeyPairProvider(settingManager);
        Iterable<KeyPair> keysIterable = provider.loadKeys();
        
        KeyPair keyPair = keysIterable.iterator().next();
        
        //encode private key again
        StringWriter privateWriter = new StringWriter();
        PemWriter privatePemWriter = new PemWriter(privateWriter);
        
        privatePemWriter.writeObject(new PemObject("RSA PRIVATE KEY", keyPair.getPrivate().getEncoded()));
        privatePemWriter.flush();
        privatePemWriter.close();
        
        assertEquals(sshSettings.getPrivateKey(), privateWriter.toString());
        
    }
}
