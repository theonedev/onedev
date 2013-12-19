package com.pmease.gitop.web.component.wiki;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.pmease.gitop.web.page.project.source.blob.renderer.highlighter.HighlightJsResourceReference;
import com.pmease.gitop.web.util.MarkdownUtils;

public class WikiTextPanel extends Panel {
	private static final long serialVersionUID = 1L;

	private final IModel<WikiType> langModel;
	
	public WikiTextPanel(String id, IModel<String> model, IModel<WikiType> lang) {
		super(id, model);
		this.langModel = lang;
	}

	@SuppressWarnings("serial")
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("wiki", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				String original = getOriginalContent();
				if (Strings.isNullOrEmpty(original)) {
					return "";
				}
				
				WikiType lang = langModel.getObject();
				if (lang == null) {
					return original;
				}
				
				String html;
				try {
					html = MarkdownUtils.transformMarkdown(original); //new Markdown4jProcessor().process(original);
					return html;
				} catch (Exception e) {
					throw Throwables.propagate(e);
				} 
			}
		}).setEscapeModelStrings(false));
	}
	
	private String getOriginalContent() {
		return (String) getDefaultModelObject();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(HighlightJsResourceReference.getInstance()));
		response.render(OnDomReadyHeaderItem.forScript(
				("$('.wiki pre code').each(function(i, e) { hljs.highlightBlock(e)});")));

	}
	
	@Override
	public void onDetach() {
		if (langModel != null) {
			langModel.detach();
		}
		
		super.onDetach();
	}
}
