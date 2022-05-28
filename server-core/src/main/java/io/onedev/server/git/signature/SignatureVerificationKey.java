package io.onedev.server.git.signature;

import org.bouncycastle.openpgp.PGPPublicKey;

public interface SignatureVerificationKey {

	boolean shouldVerifyDataWriter();
	
	PGPPublicKey getPublicKey();
	
	String getEmailAddress();
	
}
