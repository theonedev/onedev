package io.onedev.server.web.component.link.copytoclipboard;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.unbescape.javascript.JavaScriptEscape;

public class CopyToClipboardLink extends WebMarkupContainer {

	private static final long serialVersionUID = 1L;

	private final IModel<String> textModel;
	
	public CopyToClipboardLink(String id, IModel<String> textModel) {
		super(id);
		this.textModel = textModel;
	}

	@Override
	protected void onDetach() {
		textModel.detach();
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new CopyToClipboardResourceReference()));
		String script = String.format("onedev.server.copyToClipboard.onDomReady('%s', '%s');", 
				getMarkupId(true), JavaScriptEscape.escapeJavaScript(textModel.getObject()));
		response.render(OnDomReadyHeaderItem.forScript(script));
	}
	
}
