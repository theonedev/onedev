package io.onedev.server.web.page.project.blob.render.renderers.markdown;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.Model;

import io.onedev.server.git.Blob;
import io.onedev.server.web.component.markdown.MarkdownViewer;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.view.BlobViewPanel;

@SuppressWarnings("serial")
public class MarkdownBlobViewPanel extends BlobViewPanel {

	public MarkdownBlobViewPanel(String id, BlobRenderContext context) {
		super(id, context);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Blob blob = context.getProject().getBlob(context.getBlobIdent(), true);
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
		response.render(CssHeaderItem.forReference(new MarkdownBlobCssResourceReference()));
	}

	@Override
	protected boolean isEditSupported() {
		return true;
	}

	@Override
	protected boolean isViewPlainSupported() {
		return false;
	}

}
