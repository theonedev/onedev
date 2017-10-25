package com.gitplex.server.web.page.project.blob.render.renderers.source;

import java.nio.charset.Charset;

import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.Model;

import com.gitplex.server.util.ContentDetector;
import com.gitplex.utils.StringUtils;
import com.google.common.base.Charsets;

@SuppressWarnings("serial")
class SourceFormComponent extends FormComponentPanel<byte[]> {

	private TextArea<String> input;

	private final String charset;
	
	public SourceFormComponent(String id, byte[] initialContent) {
		super(id, Model.of(initialContent));
		
		Charset detectedCharset = ContentDetector.detectCharset(getModelObject());
		charset = (detectedCharset!=null?detectedCharset:Charset.defaultCharset()).name();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		String source = new String(getModelObject(), Charset.forName(charset));
		add(input = new TextArea<String>("input", Model.of(source)) {

			@Override
			protected boolean shouldTrimInput() {
				return false;
			}
			
		});
		setOutputMarkupId(true);
	}

	@Override
	public void convertInput() {
		String content = input.getConvertedInput();
		if (content != null) {
			/*
			 * Textarea always uses CRLF as line ending, and below we change back to original EOL
			 */
			String initialContent = input.getModelObject();
			if (initialContent == null || !initialContent.contains("\r\n"))
				content = StringUtils.replace(content, "\r\n", "\n");
			setConvertedInput(content.getBytes(Charsets.UTF_8));
		} else {
			setConvertedInput(new byte[0]);
		}
	}
	
}
