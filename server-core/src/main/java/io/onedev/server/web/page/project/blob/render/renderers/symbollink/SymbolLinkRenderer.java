package io.onedev.server.web.page.project.blob.render.renderers.symbollink;

import org.apache.wicket.Component;

import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext.Mode;
import io.onedev.server.web.page.project.blob.render.BlobRenderer;

public class SymbolLinkRenderer implements BlobRenderer {

	private static final long serialVersionUID = 1L;

	@Override
	public Component render(String componentId, BlobRenderContext context) {
		if (context.getMode() == Mode.VIEW && context.getBlobIdent().isSymbolLink()) 
			return new SymbolLinkPanel(componentId, context);
		else 
			return null;
	}

}
