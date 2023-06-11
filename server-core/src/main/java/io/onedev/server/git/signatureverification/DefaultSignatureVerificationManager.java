package io.onedev.server.git.signatureverification;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.util.RawParseUtils;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Set;

@Singleton
public class DefaultSignatureVerificationManager implements SignatureVerificationManager {
	
	private final Set<SignatureVerifier> signatureVerifiers;

	@Inject
	public DefaultSignatureVerificationManager(Set<SignatureVerifier> signatureVerifiers) {
		this.signatureVerifiers = signatureVerifiers;
	}

	@Nullable
	@Override
	public VerificationResult verifySignature(RevObject object) {
		if (object instanceof RevCommit) 
			return verifyCommitSignature(((RevCommit) object).getRawBuffer());
		else if (object instanceof RevTag) 
			return verifyTagSignature(((RevTag) object).getRawBuffer());
		else 
			return null;
	}

	@Nullable
	@Override
	public VerificationResult verifyCommitSignature(byte[] rawCommit) {
		byte[] header = {'g', 'p', 'g', 's', 'i', 'g'};
		int start = RawParseUtils.headerStart(header, rawCommit, 0);
		if (start < 0)
			return null;
		int end = RawParseUtils.headerEnd(rawCommit, start);
		byte[] signatureData = Arrays.copyOfRange(rawCommit, start, end);

		// start is at the beginning of the header's content
		start -= header.length + 1;
		// end is on the terminating LF; we need to skip that, too
		if (end < rawCommit.length) {
			end++;
		}
		byte[] data = new byte[rawCommit.length - (end - start)];
		System.arraycopy(rawCommit, 0, data, 0, start);
		System.arraycopy(rawCommit, end, data, start, rawCommit.length - end);

		int nameB = RawParseUtils.committer(rawCommit, 0);
		if (nameB < 0)
			return null;
		PersonIdent committerIdent = RawParseUtils.parsePersonIdent(rawCommit, nameB);
		String emailAddress = committerIdent.getEmailAddress();
		
		for (var signatureVerifier: signatureVerifiers) {
			var signatureStartBytes = Constants.encodeASCII(signatureVerifier.getPrefix());
			if (RawParseUtils.match(signatureData, 0, signatureStartBytes) != -1) 
				return signatureVerifier.verify(data, signatureData, emailAddress);
		}
		return null;
	}

	@Nullable
	@Override
	public VerificationResult verifyTagSignature(byte[] rawTag) {
		PersonIdent taggerIdent = TagParser.getTaggerIdent(rawTag);
		if (taggerIdent == null)
			return null;

		for (var signatureVerifier: signatureVerifiers) {
			byte[] signatureStart = Constants.encodeASCII(signatureVerifier.getPrefix());
			byte[] signatureData = TagParser.getRawSignature(rawTag, signatureStart);
			if (signatureData != null) {
				// The signature is just tacked onto the end of the message, which
				// is last in the buffer.
				byte[] data = Arrays.copyOfRange(rawTag, 0, rawTag.length - signatureData.length);
				return signatureVerifier.verify(data, signatureData, taggerIdent.getEmailAddress());
			}
		}		
		return null;
	}

	/*
	 * Copied from JGit
	 */
	private static class TagParser {
	
		public static int nextStart(byte[] prefix, byte[] buffer, int from) {
			int stop = buffer.length - prefix.length + 1;
			int ptr = from;
			if (ptr > 0) {
				ptr = RawParseUtils.nextLF(buffer, ptr - 1);
			}
			while (ptr < stop) {
				int lineStart = ptr;
				boolean found = true;
				for (byte element : prefix) {
					if (element != buffer[ptr++]) {
						found = false;
						break;
					}
				}
				if (found) {
					return lineStart;
				}
				do {
					ptr = RawParseUtils.nextLF(buffer, ptr);
				} while (ptr < stop && buffer[ptr] == '\n');
			}
			return -1;
		}
	
		public static int getSignatureStart(byte[] raw, byte[] signatureStart) {
			int msgB = RawParseUtils.tagMessage(raw, 0);
			if (msgB < 0) {
				return msgB;
			}
	
			// Find the last signature start and return the rest
			int start = nextStart(signatureStart, raw, msgB);
			if (start < 0) {
				return start;
			}
			int next = RawParseUtils.nextLF(raw, start);
			while (next < raw.length) {
				int newStart = nextStart(signatureStart, raw, next);
				if (newStart < 0) {
					break;
				}
				start = newStart;
				next = RawParseUtils.nextLF(raw, start);
			}
			return start;
		}
	
		public static byte[] getRawSignature(byte[] raw, byte[] signatureStart) {
			int start = getSignatureStart(raw, signatureStart);
			if (start < 0) {
				return null;
			}
			return Arrays.copyOfRange(raw, start, raw.length);
		}
	
		public static PersonIdent getTaggerIdent(byte[] raw) {
			int nameB = RawParseUtils.tagger(raw, 0);
			if (nameB < 0)
				return null;
			return RawParseUtils.parsePersonIdent(raw, nameB);
		}
	
	}
}
