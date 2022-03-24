package io.onedev.server.git.signature;

import javax.annotation.Nullable;

public interface SignatureVerificationKeyLoader {

	@Nullable
	SignatureVerificationKey getSignatureVerificationKey(long keyId);
	
}
