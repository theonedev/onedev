package io.onedev.server.crypto;

import java.io.IOException;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import javax.inject.Singleton;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import io.onedev.server.model.support.administration.SshSettings;

@Singleton
public class DefaultServerKeyPairPopulator implements ServerKeyPairPopulator {

    @Override
    public void populateSettings(SshSettings sshSettings) {
        try (StringWriter privateWriter = new StringWriter();
             PemWriter privatePemWriter = new PemWriter(privateWriter);
        ) {
            KeyPair keyPair = KeyUtils.generateKeyPair("ssh-rsa", 4096);
            
            privatePemWriter.writeObject(new PemObject("RSA PRIVATE KEY", keyPair.getPrivate().getEncoded()));
            privatePemWriter.flush();
            
            sshSettings.setPrivateKey(privateWriter.toString());
            
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
