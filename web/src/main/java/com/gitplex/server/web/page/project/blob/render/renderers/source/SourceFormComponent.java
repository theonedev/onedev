package com.gitplex.server.web.page.project.blob.render.renderers.source;

import java.nio.charset.Charset;

import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.Model;

import com.gitplex.server.util.ContentDetector;

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
		add(input = new TextArea<String>("input", Model.of(source)));
		setOutputMarkupId(true);
	}

	@Override
	public void convertInput() {
		String content = input.getConvertedInput();
		if (content != null)
			setConvertedInput(content.getBytes(Charset.forName(charset)));
		else
			setConvertedInput(new byte[0]);
	}
	
}
