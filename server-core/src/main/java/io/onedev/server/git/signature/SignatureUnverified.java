package io.onedev.server.git.signature;

import javax.annotation.Nullable;

public class SignatureUnverified extends SignatureVerification {

	private final String errorMessage;
	
	public SignatureUnverified(@Nullable SignatureVerificationKey key, String errorMessage) {
		super(key);
		this.errorMessage = errorMessage;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

}
