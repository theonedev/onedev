package io.onedev.server.entitymanager;

import javax.annotation.Nullable;

import io.onedev.server.model.GpgKey;
import io.onedev.server.persistence.dao.EntityManager;

public interface GpgKeyManager extends EntityManager<GpgKey> {

    @Nullable
    GpgKey findByKeyId(long keyId);
    
}
