package io.onedev.server.web.page.project.blob.render.renderers.image;

import org.apache.wicket.Component;

import io.onedev.server.web.PrioritizedComponentRenderer;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.BlobRendererContribution;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext.Mode;

public class ImageRendererProvider implements BlobRendererContribution {

	private static final long serialVersionUID = 1L;

	@Override
	public PrioritizedComponentRenderer getRenderer(BlobRenderContext context) {
		if (context.getMode() == Mode.VIEW && context.getBlobIdent().isFile()) {
			if (context.getProject().getBlob(context.getBlobIdent(), true).getMediaType().getType().equalsIgnoreCase("image")) {
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
