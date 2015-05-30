package com.pmease.commons.wicket.component.copyclipboard;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

@SuppressWarnings("serial")
public class CopyClipboard extends WebMarkupContainer {

	public CopyClipboard(String id, IModel<String> model) {
		super(id, model);
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		tag.put("data-clipboard-text", getDefaultModelObjectAsString());
		tag.put("title", "Click to copy");
		tag.put("data-placement", "left");
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(CopyClipboard.class, "ZeroClipboard.js")));
		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(CopyClipboard.class, "copy-clipboard.js")));

		String script = String.format("gitplex.copyClipboard('%s');", getMarkupId(true));
		response.render(OnDomReadyHeaderItem.forScript(script));
	}
	
}
