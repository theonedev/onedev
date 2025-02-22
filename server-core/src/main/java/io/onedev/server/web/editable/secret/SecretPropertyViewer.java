package io.onedev.server.web.editable.secret;

import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

public class SecretPropertyViewer extends Panel {

	private final String secret;
	
	private final int displayChars;
	
	public SecretPropertyViewer(String id, String secret, int displayChars) {
		super(id);
		this.secret = secret;
		this.displayChars = displayChars;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new Label("maskedValue", secret.substring(0, displayChars) + "************"));
		add(new CopyToClipboardLink("copy", Model.of(secret)));
	}
}
