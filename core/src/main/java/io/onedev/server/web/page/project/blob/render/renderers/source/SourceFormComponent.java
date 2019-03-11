package io.onedev.server.web.page.project.blob.render.renderers.source;

import java.nio.charset.Charset;

import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.Model;

import com.google.common.base.Charsets;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.util.ContentDetector;

@SuppressWarnings("serial")
class SourceFormComponent extends FormComponentPanel<byte[]> {

	private TextArea<String> input;

	public SourceFormComponent(String id, byte[] initialContent) {
		super(id, Model.of(initialContent));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Charset detectedCharset = ContentDetector.detectCharset(getModelObject());
		Charset charset = detectedCharset!=null?detectedCharset:Charset.defaultCharset();
		
		String source = new String(getModelObject(), charset);
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
