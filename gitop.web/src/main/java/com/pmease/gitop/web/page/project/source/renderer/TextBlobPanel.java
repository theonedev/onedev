package com.pmease.gitop.web.page.project.source.renderer;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.Strings;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.pmease.gitop.web.page.project.source.GitBlob;
import com.pmease.gitop.web.service.impl.Language;
import com.pmease.gitop.web.util.MimeTypeUtils;

@SuppressWarnings("serial")
public class TextBlobPanel extends Panel {

	public TextBlobPanel(String id, IModel<GitBlob> model) {
		super(id, model);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("lineno", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				String content = getBlob().getStringContent();
				Iterable<String> it = Splitter.on("\n").split(content);
				int size = Iterables.size(it);
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < size; i++) {
					int lineno = i + 1;
					sb.append("<a href='#L" + lineno + "' id='L" + lineno + "'>" + lineno + "</a>").append("\n");
				}
				return sb.toString();
			}
			
		}).setEscapeModelStrings(false));
		
		add(new Label("code", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				String s = getBlob().getStringContent();
				if (Strings.isEmpty(s)) {
					return "This file is empty";
				} else {
					return s;
				}
			}
			
		}).setOutputMarkupId(true)
			.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

				@Override
				public String getObject() {
					GitBlob blob = getBlob();
					if (blob.getSize() == 0) {
						return "no-highlight";
					}
					
					Language language = blob.getLanguage();
					if (language != null) {
						if (language.getLanguageType() != null && language.getLanguageType() == Language.Type.DATA) {
							return "no-highlight";
						} else {
							return language.getHighlightCss() != null ?
									language.getHighlightCss() 
									: language.getId().toLowerCase();
						}
					}
					
					String type = MimeTypeUtils.guessSourceType(getBlob().getMime());
					return type == null ? "no-highlight" : type;
				}
				
			}))
			.add(new HighlightBehavior()));
		
	}
	
	private GitBlob getBlob() {
		return (GitBlob) getDefaultModelObject();
	}
	
	
}
