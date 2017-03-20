package com.gitplex.server.web.page.depot.blob.render.renderers.markdown;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.Model;

import com.gitplex.server.web.component.markdown.BlobReferenceSupport;
import com.gitplex.server.web.component.markdown.MarkdownEditor;
import com.google.common.base.Charsets;

@SuppressWarnings("serial")
abstract class MarkdownBlobEditor extends FormComponentPanel<byte[]> {

	private final boolean autoFocus;
	
	private MarkdownEditor input;
	
	public MarkdownBlobEditor(String id, byte[] initialContent, boolean autoFocus) {
		super(id, Model.of(initialContent));
		this.autoFocus = autoFocus;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(input = new MarkdownEditor("input", Model.of(new String(getModelObject(), Charsets.UTF_8)), false) {

			@Override
			protected BlobReferenceSupport getBlobReferenceSupport() {
				return MarkdownBlobEditor.this.getBlobReferenceSupport();
			}
			
		});
		input.setOutputMarkupId(true);
	}

	@Override
	public void convertInput() {
		String content = input.getConvertedInput();
		if (content != null)
			setConvertedInput(content.getBytes());
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
}
