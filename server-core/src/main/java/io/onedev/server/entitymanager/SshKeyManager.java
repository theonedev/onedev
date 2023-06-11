package io.onedev.server.entitymanager;

import java.util.Collection;

import javax.annotation.Nullable;

import io.onedev.server.model.SshKey;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

public interface SshKeyManager extends EntityManager<SshKey> {

    @Nullable
    SshKey findByFingerprint(String fingerprint);
    
    void syncSshKeys(User user, Collection<String> sshKeys);

    void create(SshKey sshKey);
	
}
