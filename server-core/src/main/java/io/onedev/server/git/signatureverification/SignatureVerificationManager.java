package io.onedev.server.git.signatureverification;

import org.eclipse.jgit.revwalk.RevObject;

import javax.annotation.Nullable;

public interface SignatureVerificationManager {
	
	@Nullable
	VerificationResult verifySignature(RevObject object);

	@Nullable
	VerificationResult verifyCommitSignature(byte[] rawCommit);

	@Nullable
	VerificationResult verifyTagSignature(byte[] rawTag);
	
}
