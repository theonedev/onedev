package io.onedev.server.git.signature;

import java.util.List;

import org.bouncycastle.openpgp.PGPPublicKey;

public interface SignatureVerificationKey {

	boolean shouldVerifyDataWriter();
	
	PGPPublicKey getPublicKey();
	
	List<String> getEmailAddresses();
	
}
