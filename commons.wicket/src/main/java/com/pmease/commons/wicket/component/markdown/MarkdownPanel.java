package com.pmease.commons.wicket.component.markdown;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.markdown.MarkdownManager;
import com.pmease.commons.wicket.behavior.markdown.MarkdownBehavior;

@SuppressWarnings("serial")
public class MarkdownPanel extends GenericPanel<String> {
	private static final long serialVersionUID = 1L;

	public MarkdownPanel(String id, IModel<String> model) {
		super(id, model);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("html", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return AppLoader.getInstance(MarkdownManager.class)
						.toHtml(MarkdownPanel.this.getModelObject(), true, true);
			}
		}).setEscapeModelStrings(false));

	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new CssResourceReference(MarkdownBehavior.class, "markdown.css")));
	}
	
}
