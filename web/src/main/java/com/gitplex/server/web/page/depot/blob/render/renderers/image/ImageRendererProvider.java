package com.gitplex.server.web.page.depot.blob.render.renderers.image;

import org.apache.wicket.Component;

import com.gitplex.server.web.PrioritizedComponentRenderer;
import com.gitplex.server.web.page.depot.blob.render.BlobRenderContext;
import com.gitplex.server.web.page.depot.blob.render.BlobRenderContext.Mode;
import com.gitplex.server.web.page.depot.blob.render.BlobRendererContribution;

public class ImageRendererProvider implements BlobRendererContribution {

	private static final long serialVersionUID = 1L;

	@Override
	public PrioritizedComponentRenderer getRenderer(BlobRenderContext context) {
		if (context.getMode() == Mode.VIEW && context.getBlobIdent().isFile()) {
			if (context.getDepot().getBlob(context.getBlobIdent()).getMediaType().getType().equalsIgnoreCase("image")) {
				return new PrioritizedComponentRenderer() {

					private static final long serialVersionUID = 1L;

					@Override
					public Component render(String componentId) {
						return new ImageViewPanel(componentId, context);
					}

					@Override
					public int getPriority() {
						return 0;
					}
					
				};
			}
		} 
		return null;
	}

}
