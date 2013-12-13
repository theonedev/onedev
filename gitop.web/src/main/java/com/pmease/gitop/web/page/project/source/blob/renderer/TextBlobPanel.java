package com.pmease.gitop.web.page.project.source.blob.renderer;

import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.web.page.project.source.blob.FileBlob;
import com.pmease.gitop.web.page.project.source.blob.renderer.syntax.HighlightBehavior;
import com.pmease.gitop.web.service.impl.Language;

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
					sb.append("<span id='L" + lineno + "'>" + lineno + "</span>").append("\n");
				}
				return sb.toString();
			}
			
		}).setEscapeModelStrings(false));
		
		IModel<String> langModel = new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				Language lang = getBlob().getLanguage();
				if (lang == null) {
					return "no-highlight";
				}
				
				return lang.getHighlightType();
			}
			
		};
		
		IModel<String> textModel = new AbstractReadOnlyModel<String>() {
			@Override
			public String getObject() {
				if (getBlob().isEmpty()) {
					return "This file is empty";
				}
				
//				Language language = getBlob().getLanguage();
//				String type = language == null ? "text" : language.getHighlightType();
//				
//				Renderer r = XhtmlRendererFactory.getRenderer(type);
//				if (r == null) {
//					return "<pre>" + getBlob().getStringContent() + "</pre>";
//				}
//				
//				InputStream in = null;
//				ByteArrayOutputStream out = null;
//				try {
//					Charset charset = getBlob().getCharset();
//					in = new ByteArrayInputStream(getBlob().getStringContent().getBytes(charset));
//					out = new ByteArrayOutputStream();
//					
//					r.highlight("",
//							in,
//							out,
//							charset.name(),
//							true);
//					return out.toString(charset.name());
//				} catch (IOException e) {
//					throw Throwables.propagate(e);
//				} finally {
//					IOUtils.closeQuietly(in);
//					IOUtils.closeQuietly(out);
//				}
				
				return getBlob().getStringContent();
			}
		};
		
		Label code = new Label("code", textModel);
		code.setOutputMarkupId(true);

		code.add(AttributeAppender.append("class", langModel));
		if (getBlob().canHighlight()) {
			code.add(new HighlightBehavior(langModel));
		}
		
		add(code);
		
	}
	
	private FileBlob getBlob() {
		return (FileBlob) getDefaultModelObject();
	}
	
	
}
