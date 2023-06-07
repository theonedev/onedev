package io.onedev.server.git.signatureverification;

import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTag;

import javax.annotation.Nullable;
import java.io.Serializable;

public interface SignatureVerifier extends Serializable {
	
	@Nullable
	VerificationResult verify(byte[] data, byte[] signatureData, String emailAddress);
	
}
