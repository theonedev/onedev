package io.onedev.server.web.component.gitsignature;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.util.GpgUtils;
import io.onedev.server.web.component.MultilineLabel;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import java.util.List;

@SuppressWarnings("serial")
public class GpgVerificationDetailPanel extends Panel {

	private final boolean successful;

	private final String message;
	
	private final long keyId;
	
	private final List<String> emailAddresses;
	
	public GpgVerificationDetailPanel(String id, boolean successful, String message, long keyId, List<String> emailAddresses) {
		super(id);
		this.successful = successful;
		this.keyId = keyId;
		this.emailAddresses = emailAddresses;
		this.message = message;
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

		add(new Label("keyId", GpgUtils.getKeyIDString(keyId)));
		add(new MultilineLabel("emailAddresses", StringUtils.join(emailAddresses, "\n")));
	}
	
}
