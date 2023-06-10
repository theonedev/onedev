package io.onedev.server.git.signatureverification.gpg;

import io.onedev.server.git.signatureverification.VerificationFailed;
import io.onedev.server.web.component.gitsignature.GpgVerificationDetailPanel;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.eclipse.jgit.revwalk.RevObject;

import javax.annotation.Nullable;

import static io.onedev.server.util.GpgUtils.getEmailAddresses;

public class GpgVerificationFailed implements VerificationFailed {
	
	private final GpgSigningKey signingKey;
	
	private final String errorMessage;
	
	public GpgVerificationFailed(@Nullable GpgSigningKey signingKey, String errorMessage) {
		this.signingKey = signingKey;
		this.errorMessage = errorMessage;
	}

	@Override
	public Component renderDetail(String componentId, RevObject revObject) {
		if (signingKey != null) {
			var publicKey = signingKey.getPublicKey();
			return new GpgVerificationDetailPanel(componentId, false, errorMessage, 
					publicKey.getKeyID(), getEmailAddresses(publicKey));
		} else {
			return new Label(componentId, errorMessage)
					.add(AttributeAppender.append("class", "p-4"));
		}
	}
}
