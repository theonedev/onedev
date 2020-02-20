package io.onedev.server.crypto;

import io.onedev.server.model.support.administration.SshSettings;

@FunctionalInterface
public interface ServerKeyPairPopulator {
    public void populateSettings(SshSettings sshSettings);
}
