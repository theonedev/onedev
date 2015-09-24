package com.pmease.commons.wicket.component.markdown;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.markdown.MarkdownManager;
import com.pmease.commons.wicket.assets.codemirror.HighlightResourceReference;
import com.pmease.commons.wicket.behavior.markdown.MarkdownCssResourceReference;

@SuppressWarnings("serial")
public class MarkdownViewer extends GenericPanel<String> {
	private static final long serialVersionUID = 1L;

	public MarkdownViewer(String id, IModel<String> model) {
		super(id, model);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("html", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return AppLoader.getInstance(MarkdownManager.class)
						.parseAndProcess(MarkdownViewer.this.getModelObject());
			}
		}).setEscapeModelStrings(false));

	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(HighlightResourceReference.INSTANCE));
		response.render(CssHeaderItem.forReference(MarkdownCssResourceReference.INSTANCE));
		
		String script = String.format("pmease.commons.highlight($('#%s>.md-preview'));", getMarkupId(true));
		response.render(OnDomReadyHeaderItem.forScript(script));
	}
	
}
