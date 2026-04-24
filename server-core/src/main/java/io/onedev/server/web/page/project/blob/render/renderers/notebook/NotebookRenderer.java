package io.onedev.server.web.page.project.blob.render.renderers.notebook;

import org.apache.wicket.Component;

import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext.Mode;
import io.onedev.server.web.page.project.blob.render.BlobRenderer;

public class NotebookRenderer implements BlobRenderer {

	private static final long serialVersionUID = 1L;

	@Override
	public Component render(String componentId, BlobRenderContext context) {
		String path = context.getBlobIdent().path;
		if (context.getMode() == Mode.VIEW && path != null && path.toLowerCase().endsWith(".ipynb")) {
			return new NotebookViewPanel(componentId, context);
		} else {
			return null;
		}
	}

}
