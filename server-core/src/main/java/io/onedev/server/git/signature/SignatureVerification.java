package io.onedev.server.git.signature;

import javax.annotation.Nullable;

public abstract class SignatureVerification {

	private final SignatureVerificationKey key;
	
	public SignatureVerification(@Nullable SignatureVerificationKey key) {
		this.key = key;
	}
	
	@Nullable
	public SignatureVerificationKey getVerificationKey() {
		return key;
	}
	
}
