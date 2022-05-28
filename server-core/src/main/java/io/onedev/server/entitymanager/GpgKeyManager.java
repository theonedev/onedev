package io.onedev.server.entitymanager;

import javax.annotation.Nullable;

import io.onedev.server.git.signature.SignatureVerificationKey;
import io.onedev.server.model.GpgKey;
import io.onedev.server.persistence.dao.EntityManager;

public interface GpgKeyManager extends EntityManager<GpgKey> {

    @Nullable
    SignatureVerificationKey findSignatureVerificationKey(long keyId);
    
}
