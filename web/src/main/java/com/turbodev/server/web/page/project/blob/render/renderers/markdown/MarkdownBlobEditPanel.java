package com.turbodev.server.web.page.project.blob.render.renderers.markdown;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.FormComponentPanel;

import com.turbodev.server.web.page.project.blob.render.BlobRenderContext;
import com.turbodev.server.web.page.project.blob.render.edit.BlobEditPanel;

@SuppressWarnings("serial")
public class MarkdownBlobEditPanel extends BlobEditPanel {

	public MarkdownBlobEditPanel(String id, BlobRenderContext context) {
		super(id, context);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new MarkdownBlobResourceReference()));
	}

	@Override
	protected FormComponentPanel<byte[]> newContentEditor(String componentId, byte[] initialContent) {
		return new MarkdownBlobEditor(componentId, context, initialContent) {

			@Override
			protected String getAutosaveKey() {
				return context.getAutosaveKey();
			}

		};
	}

}
