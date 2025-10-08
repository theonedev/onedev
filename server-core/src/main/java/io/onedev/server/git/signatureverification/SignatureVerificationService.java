package io.onedev.server.git.signatureverification;

import org.eclipse.jgit.revwalk.RevObject;

import org.jspecify.annotations.Nullable;

public interface SignatureVerificationService {
	
	@Nullable
	VerificationResult verifySignature(RevObject object);

	@Nullable
	VerificationResult verifyCommitSignature(byte[] rawCommit);

	@Nullable
	VerificationResult verifyTagSignature(byte[] rawTag);
	
}
