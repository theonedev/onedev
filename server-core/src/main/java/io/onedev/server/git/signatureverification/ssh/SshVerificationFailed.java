package io.onedev.server.git.signatureverification.ssh;

import io.onedev.server.git.signatureverification.VerificationFailed;
import io.onedev.server.web.component.gitsignature.SshVerificationDetailPanel;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.eclipse.jgit.revwalk.RevObject;

import javax.annotation.Nullable;

public class SshVerificationFailed implements VerificationFailed {
	
	private final SshKeyInfo signingKey;
	
	private final String errorMessage;
	
	public SshVerificationFailed(@Nullable SshKeyInfo signingKey, String errorMessage) {
		this.signingKey = signingKey;
		this.errorMessage = errorMessage;
	}

	@Override
	public Component renderDetail(String componentId, RevObject revObject) {
		if (signingKey != null) {
			return new SshVerificationDetailPanel(componentId, false, 
					errorMessage, signingKey);
		} else {
			return new Label(componentId, errorMessage)
					.add(AttributeAppender.append("class", "p-4"));
		}
	}
}
