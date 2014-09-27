package com.pmease.gitplex.web.component.wiki;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.google.common.base.Throwables;
import com.pmease.gitplex.web.page.repository.code.blob.renderer.highlighter.AceHighlighter;
import com.pmease.gitplex.web.util.MarkdownUtils;

@SuppressWarnings("serial")
public class WikiTextPanel extends Panel {
	private static final long serialVersionUID = 1L;

	public WikiTextPanel(String id, IModel<String> contentModel) {
		super(id, contentModel);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("wiki", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				String markdown = WikiTextPanel.this.getDefaultModelObjectAsString();
				String html;
				try {
					html = MarkdownUtils.transformMarkdown(markdown);
					return html;
				} catch (Exception e) {
					throw Throwables.propagate(e);
				} 
			}
		}).setEscapeModelStrings(false));
		
		add(new AceHighlighter().withSelector("#" + getMarkupId() + " pre code").hasLineId(false));
	}
	
}
