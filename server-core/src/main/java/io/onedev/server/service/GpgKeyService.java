package io.onedev.server.service;

import org.jspecify.annotations.Nullable;

import io.onedev.server.git.signatureverification.gpg.GpgSigningKey;
import io.onedev.server.model.GpgKey;

public interface GpgKeyService extends EntityService<GpgKey> {

    @Nullable
	GpgSigningKey findSigningKey(long keyId);

    void create(GpgKey gpgKey);
}
