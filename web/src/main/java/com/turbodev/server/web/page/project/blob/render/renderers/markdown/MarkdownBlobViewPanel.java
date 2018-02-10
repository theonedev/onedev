package com.turbodev.server.web.page.project.blob.render.renderers.markdown;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.Model;

import com.turbodev.server.git.Blob;
import com.turbodev.server.web.component.markdown.MarkdownViewer;
import com.turbodev.server.web.page.project.blob.render.BlobRenderContext;
import com.turbodev.server.web.page.project.blob.render.view.BlobViewPanel;

@SuppressWarnings("serial")
public class MarkdownBlobViewPanel extends BlobViewPanel {

	public MarkdownBlobViewPanel(String id, BlobRenderContext context) {
		super(id, context);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Blob blob = context.getProject().getBlob(context.getBlobIdent());
		add(new MarkdownViewer("markdown", Model.of(blob.getText().getContent()), null) {

			@Override
			protected Object getRenderContext() {
				return context;
			}
			
		});
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new MarkdownBlobResourceReference()));
	}

	@Override
	protected boolean isEditSupported() {
		return true;
	}

	@Override
	protected boolean isBlameSupported() {
		return true;
	}

}
