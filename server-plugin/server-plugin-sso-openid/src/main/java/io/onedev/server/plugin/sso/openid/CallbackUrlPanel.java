package io.onedev.server.plugin.sso.openid;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;

@SuppressWarnings("serial")
public class CallbackUrlPanel extends Panel {

	private final String callbackUrl;
	
	public CallbackUrlPanel(String id, String callbackUrl) {
		super(id);
		this.callbackUrl = callbackUrl;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new Label("url", callbackUrl));
		add(new CopyToClipboardLink("copy", Model.of(callbackUrl)));
	}

}
