package io.onedev.server.web.component.codesnippet;

import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import io.onedev.server.web.component.svg.SpriteImage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

public class CodeSnippetPanel extends GenericPanel<String> {

	public CodeSnippetPanel(String id, IModel<String> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new Label("codeSnippet", getModel()));
		var link = new CopyToClipboardLink("copy", getModel());
		link.add(new SpriteImage("icon", "copy"));
		add(link);
	}

}
