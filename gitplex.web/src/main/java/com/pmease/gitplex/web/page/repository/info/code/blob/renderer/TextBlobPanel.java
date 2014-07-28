package com.pmease.gitplex.web.page.repository.info.code.blob.renderer;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import com.pmease.commons.util.MediaTypes;
import com.pmease.gitplex.web.page.repository.info.code.blob.language.Language;
import com.pmease.gitplex.web.page.repository.info.code.blob.language.Languages;
import com.pmease.gitplex.web.page.repository.info.code.blob.renderer.highlighter.AceHighlighter;
import com.pmease.gitplex.web.service.FileBlob;

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
				Language lang = Languages.INSTANCE.findByMediaType(getBlob().getMediaType());
				StringBuffer sb = new StringBuffer();
				if (lang == null) {
					if (MediaTypes.isXML(getBlob().getMediaType())) {
						sb.append("xml lang-xml");
					} else {
						sb.append("no-highlight lang-text");
					}
				} else {
					sb.append(lang.getMode()).append(" ").append("lang-" + lang.getAceMode());
				}
				
				return sb.toString();
			}
		};
		
		code.add(AttributeAppender.append("class", cssModel));
		code.add(new AceHighlighter());
		add(code);
		
		add(createPrependColumn("prepend"));
	}
	
	protected Component createPrependColumn(String id) {
		return new WebMarkupContainer(id).setVisibilityAllowed(false);
	}
	
	protected FileBlob getBlob() {
		return (FileBlob) getDefaultModelObject();
	}
	
}
