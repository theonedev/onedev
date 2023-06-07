package io.onedev.server.git.signatureverification.ssh;

import io.onedev.server.git.signatureverification.VerificationSuccessful;
import io.onedev.server.web.component.gitsignature.SshVerificationDetailPanel;
import org.apache.wicket.Component;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;

public class SshVerificationSuccessful implements VerificationSuccessful {
	
	private final SshKeyInfo signingKey;
	
	public SshVerificationSuccessful(SshKeyInfo signingKey) {
		this.signingKey = signingKey;
	}
	
	@Override
	public Component renderDetail(String componentId, RevObject revObject) {
		String message;
		if (revObject instanceof RevCommit)
			message = "Signature verified successfully with committer's SSH key";
		else
			message = "Signature verified successfully with tagger's SSH key";
		return new SshVerificationDetailPanel(componentId, true, 
				message, signingKey);
	}
	
}
