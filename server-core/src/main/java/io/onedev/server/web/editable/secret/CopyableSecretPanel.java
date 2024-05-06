package io.onedev.server.web.editable.secret;

import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

@SuppressWarnings("serial")
public class CopyableSecretPanel extends Panel {

	private final String secret;
	
	public CopyableSecretPanel(String id, String secret) {
		super(id);
		this.secret = secret;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new CopyToClipboardLink("copy", Model.of(secret)));
	}
}
