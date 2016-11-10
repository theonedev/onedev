package com.gitplex.commons.wicket.behavior.clipboard;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.model.IModel;
import org.unbescape.javascript.JavaScriptEscape;

public class CopyClipboardBehavior extends Behavior {

	private static final long serialVersionUID = 1L;

	private final IModel<String> textModel;
	
	public CopyClipboardBehavior(IModel<String> textModel) {
		this.textModel = textModel;
	}

	@Override
	public void detach(Component component) {
		textModel.detach();
		super.detach(component);
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		response.render(JavaScriptHeaderItem.forReference(new CopyClipboardResourceReference()));
		String script = String.format("gitplex.commons.copyclipboard.init('%s', '%s');", 
				component.getMarkupId(true), JavaScriptEscape.escapeJavaScript(textModel.getObject()));
		response.render(OnDomReadyHeaderItem.forScript(script));
	}
	
}
