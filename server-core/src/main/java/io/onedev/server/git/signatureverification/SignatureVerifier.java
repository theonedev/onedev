package io.onedev.server.git.signatureverification;

import java.io.Serializable;

public interface SignatureVerifier extends Serializable {
	
	VerificationResult verify(byte[] data, byte[] signatureData, String emailAddress);
	
	String getPrefix();
	
}
