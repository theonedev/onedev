package io.onedev.server.web.page.project.blob.render.renderers.markdown;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.FormComponentPanel;

import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.edit.BlobEditPanel;

@SuppressWarnings("serial")
public class MarkdownBlobEditPanel extends BlobEditPanel {

	public MarkdownBlobEditPanel(String id, BlobRenderContext context) {
		super(id, context);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new MarkdownBlobCssResourceReference()));
	}

	@Override
	protected FormComponentPanel<byte[]> newEditor(String componentId, byte[] initialContent) {
		return new MarkdownBlobEditor(componentId, context, initialContent) {

			@Override
			protected String getAutosaveKey() {
				return context.getAutosaveKey();
			}

		};
	}

}
