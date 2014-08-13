package com.pmease.gitplex.web.component.markdown;

import static org.pegdown.Extensions.ALL;
import static org.pegdown.Extensions.SMARTYPANTS;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.pegdown.LinkRenderer;
import org.pegdown.PegDownProcessor;

import com.pmease.gitplex.web.page.repository.info.code.blob.renderer.highlighter.AceHighlighter;

@SuppressWarnings("serial")
public class MarkdownPanel extends Panel {
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
				String markdown = MarkdownPanel.this.getDefaultModelObjectAsString();
				PegDownProcessor pd = new PegDownProcessor(ALL & ~SMARTYPANTS);
				return pd.markdownToHtml(markdown, new LinkRenderer());
			}
		}).setEscapeModelStrings(false));
		
		add(new AceHighlighter().withSelector("#" + getMarkupId() + " pre code").hasLineId(false));
	}
	
}
