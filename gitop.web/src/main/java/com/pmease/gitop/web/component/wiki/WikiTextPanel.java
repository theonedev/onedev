package com.pmease.gitop.web.component.wiki;

import java.io.IOException;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.markdown4j.Markdown4jProcessor;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.pmease.gitop.web.util.MarkdownUtils;

public class WikiTextPanel extends Panel {
	private static final long serialVersionUID = 1L;

	private final IModel<String> langModel;
	
	public WikiTextPanel(String id, IModel<String> model, IModel<String> lang) {
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
				
				String lang = langModel.getObject();
				if (Strings.isNullOrEmpty(lang)) {
					return original;
				}
				
				String html;
				try {
					html = MarkdownUtils.transformMarkdown(original); //new Markdown4jProcessor().process(original);
					return html;
				} catch (Exception e) {
					throw Throwables.propagate(e);
				} 
				
//				MarkupLanguage l = ServiceLocator.getInstance().getMarkupLanguage(lang);
//				MarkupParser parser = new MarkupParser(l);
//				
//				return parser.parseToHtml(original);
			}
		}).setEscapeModelStrings(false));
	}
	
	private String getOriginalContent() {
		return (String) getDefaultModelObject();
	}
	
	@Override
	public void onDetach() {
		if (langModel != null) {
			langModel.detach();
		}
		
		super.onDetach();
	}
}
