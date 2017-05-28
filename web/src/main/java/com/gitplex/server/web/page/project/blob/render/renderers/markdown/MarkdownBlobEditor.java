package com.gitplex.server.web.page.project.blob.render.renderers.markdown;

import java.nio.charset.Charset;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.Model;

import com.gitplex.server.util.ContentDetector;
import com.gitplex.server.web.component.markdown.BlobReferenceSupport;
import com.gitplex.server.web.component.markdown.MarkdownEditor;

@SuppressWarnings("serial")
abstract class MarkdownBlobEditor extends FormComponentPanel<byte[]> {

	private final boolean autoFocus;
	
	private final String charset;

	private MarkdownEditor input;
	
	public MarkdownBlobEditor(String id, byte[] initialContent, boolean autoFocus) {
		super(id, Model.of(initialContent));
		this.autoFocus = autoFocus;

		Charset detectedCharset = ContentDetector.detectCharset(getModelObject());
		charset = (detectedCharset!=null?detectedCharset:Charset.defaultCharset()).name();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(input = new MarkdownEditor("input", Model.of(new String(getModelObject(), Charset.forName(charset))), 
				false) {

			@Override
			protected BlobReferenceSupport getBlobReferenceSupport() {
				return MarkdownBlobEditor.this.getBlobReferenceSupport();
			}

			@Override
			protected String getAutosaveKey() {
				return MarkdownBlobEditor.this.getAutosaveKey();
			}

			@Override
			protected String getBaseUrl() {
				return MarkdownBlobEditor.this.getBaseUrl();
			}
			
		});
		if (!autoFocus) {
			input.add(AttributeAppender.append("class", "no-autofocus"));
		}
		input.setOutputMarkupId(true);
	}

	@Override
	public void convertInput() {
		String content = input.getConvertedInput();
		if (content != null)
			setConvertedInput(content.getBytes(Charset.forName(charset)));
		else
			setConvertedInput(new byte[0]);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		if (autoFocus) {
			String script = String.format("$('#%s textarea').focus();", input.getMarkupId());
			response.render(OnDomReadyHeaderItem.forScript(script));
		}
	}

	protected abstract BlobReferenceSupport getBlobReferenceSupport();
	
	protected abstract String getAutosaveKey();
	
	protected abstract String getBaseUrl();
}
