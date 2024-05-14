package io.onedev.server.web.editable.password;

import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

@SuppressWarnings("serial")
public class PasswordPropertyViewer extends Panel {

	private final String password;
	
	private final boolean copyable;
	
	public PasswordPropertyViewer(String id, String password, boolean copyable) {
		super(id);
		this.password = password;
		this.copyable = copyable;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new CopyToClipboardLink("copy", Model.of(password)).setVisible(copyable));
	}
}
