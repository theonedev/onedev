package io.onedev.server.entitymanager;

import java.util.List;
import io.onedev.server.model.SshKey;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

public interface SshKeyManager extends EntityManager<SshKey> {

    List<SshKey> loadUserKeys(User user);

    SshKey loadKeyByDigest(String digest);

    boolean isKeyAlreadyInUse(String keyDigest);

}
