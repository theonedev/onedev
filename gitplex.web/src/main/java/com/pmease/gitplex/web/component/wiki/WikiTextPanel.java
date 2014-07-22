package com.pmease.gitplex.web.component.wiki;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.pmease.gitplex.web.page.repository.info.code.blob.renderer.highlighter.AceHighlighter;
import com.pmease.gitplex.web.util.MarkdownUtils;

public class WikiTextPanel extends Panel {
	private static final long serialVersionUID = 1L;

	private final IModel<WikiType> langModel;
	
	private final IModel<Boolean> enableHighlightJs = Model.of(true);
	
	public WikiTextPanel(String id, IModel<String> model) {
		this(id, model, Model.of(WikiType.MARKDOWN));
	}
	
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
		
		if (isEnableHighlightJs()) {
			add(new AceHighlighter().withSelector("#" + getMarkupId() + " pre code").hasLineId(false));
		}
	}
	
	private String getOriginalContent() {
		return (String) getDefaultModelObject();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		if (isEnableHighlightJs()) {
//			response.render(JavaScriptHeaderItem.forReference(HighlightJsResourceReference.getInstance()));
//			response.render(OnDomReadyHeaderItem.forScript(
//					("$('#" + getMarkupId(true) + " .wiki pre code').each(function(i, e) { hljs.highlightBlock(e)});")));
		}
	}
	
	private boolean isEnableHighlightJs() {
		return enableHighlightJs.getObject();
	}
	
	public WikiTextPanel enableHighlightJs(Boolean b) {
		enableHighlightJs.setObject(b);
		return this;
	}
	
	@Override
	public void onDetach() {
		if (langModel != null) {
			langModel.detach();
		}
		
		super.onDetach();
	}
}
