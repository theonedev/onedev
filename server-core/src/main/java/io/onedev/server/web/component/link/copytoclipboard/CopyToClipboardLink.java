package io.onedev.server.web.component.link.copytoclipboard;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.unbescape.javascript.JavaScriptEscape;

public class CopyToClipboardLink extends WebMarkupContainer {

	private static final long serialVersionUID = 1L;

	private final IModel<String> textModel;

	private final String tooltip;
	
	public CopyToClipboardLink(String id, IModel<String> textModel) {
		this(id, textModel, _T("Copy to clipboard"));
	}

	public CopyToClipboardLink(String id, IModel<String> textModel, String tooltip) {
		super(id);
		this.textModel = textModel;
		this.tooltip = tooltip;
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
		String script = String.format("onedev.server.copyToClipboard.onDomReady('%s', '%s', '%s', '%s');",
				getMarkupId(true), 
				JavaScriptEscape.escapeJavaScript(textModel.getObject()), 
				JavaScriptEscape.escapeJavaScript(tooltip),
				JavaScriptEscape.escapeJavaScript(_T("Copied to clipboard")));
		response.render(OnDomReadyHeaderItem.forScript(script));
	}
	
}
