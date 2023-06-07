package io.onedev.server.web.component.gitsignature;

import io.onedev.server.git.signatureverification.ssh.SshKeyInfo;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

@SuppressWarnings("serial")
public class SshVerificationDetailPanel extends Panel {

	private final boolean successful;

	private final String message;
	
	private final SshKeyInfo signingKey;
	
	public SshVerificationDetailPanel(String id, boolean successful, String message, SshKeyInfo signingKey) {
		super(id);
		this.successful = successful;
		this.message = message;
		this.signingKey = signingKey;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Label messageLabel;
		add(messageLabel = new Label("message", message));
		
		if (successful)
			messageLabel.add(AttributeAppender.append("class", "alert alert-light-success mb-0"));
		else
			messageLabel.add(AttributeAppender.append("class", "alert alert-light-danger mb-0"));
		
		add(new Label("keyType", signingKey.getType()));
		add(new Label("keyFingerprint", signingKey.getFingerprint()));
	}
	
}
