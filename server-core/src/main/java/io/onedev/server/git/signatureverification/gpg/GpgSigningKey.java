package io.onedev.server.git.signatureverification.gpg;

import org.bouncycastle.openpgp.PGPPublicKey;

import org.jspecify.annotations.Nullable;
import java.util.Collection;

public interface GpgSigningKey {
	
	PGPPublicKey getPublicKey();
	
	@Nullable
	Collection<String> getEmailAddresses();
	
}
