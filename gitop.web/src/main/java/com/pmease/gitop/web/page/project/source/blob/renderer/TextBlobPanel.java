package com.pmease.gitop.web.page.project.source.blob.renderer;

import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.web.page.project.source.blob.FileBlob;
import com.pmease.gitop.web.page.project.source.blob.language.Language;
import com.pmease.gitop.web.page.project.source.blob.language.Languages;
import com.pmease.gitop.web.page.project.source.blob.renderer.highlighter.AceHighlighter;
import com.pmease.gitop.web.util.MimeTypeUtils;

@SuppressWarnings("serial")
public class TextBlobPanel extends Panel {

	public TextBlobPanel(String id, IModel<FileBlob> model) {
		super(id, model);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("lineno", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				List<String> lines = getBlob().getLines();
				int size = lines.size();
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < size; i++) {
					int lineno = i + 1;
					sb.append("<a id='LL" + lineno + "'>")
						.append(lineno)
						.append("</a>").append("\n");
				}
				return sb.toString();
			}
			
		}).setEscapeModelStrings(false));
		
		IModel<String> textModel = new AbstractReadOnlyModel<String>() {
			@Override
			public String getObject() {
				if (getBlob().isEmpty()) {
					return "This file is empty";
				}
				return getBlob().getStringContent();
			}
		};
		
		Label code = new Label("code", textModel);
		code.setOutputMarkupId(true);

		IModel<String> cssModel = new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				Language lang = Languages.INSTANCE.findByMime(getBlob().getMimeType());
				if (lang == null) {
					if (MimeTypeUtils.isXMLType(getBlob().getMimeType())) {
						return "xml";
					} else {
						return "no-highlight";
					}
				} else {
					return lang.getMode();
				}
			}
		};
		
		code.add(AttributeAppender.append("class", cssModel));
		
		IModel<String> modeModel = new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				Language language = getBlob().getLanguage();
				return language == null ? "text" : language.getAceMode(); 
			}
			
		};
		
		code.add(new AceHighlighter(modeModel));
		
		add(code);
		
	}
	
	private FileBlob getBlob() {
		return (FileBlob) getDefaultModelObject();
	}
	
	
}
