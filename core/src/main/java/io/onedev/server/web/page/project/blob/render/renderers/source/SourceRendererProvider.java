package io.onedev.server.web.page.project.blob.render.renderers.source;

import org.apache.wicket.Component;

import io.onedev.server.web.PrioritizedComponentRenderer;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.BlobRendererContribution;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext.Mode;

public class SourceRendererProvider implements BlobRendererContribution {

	private static final long serialVersionUID = 1L;

	@Override
	public PrioritizedComponentRenderer getRenderer(BlobRenderContext context) {
		if (context.getMode() == Mode.ADD 
				|| context.getMode() == Mode.EDIT 
						&& context.getProject().getBlob(context.getBlobIdent(), true).getText() != null) {
			return new PrioritizedComponentRenderer() {
				
				private static final long serialVersionUID = 1L;

				@Override
				public Component render(String componentId) {
					return new SourceEditPanel(componentId, context);
				}
				
				@Override
				public int getPriority() {
					return Integer.MAX_VALUE-1;
				}
			};
		} else if ((context.getMode() == Mode.VIEW || context.getMode() == Mode.BLAME) 
				&& context.getBlobIdent().isFile() 
				&& context.getProject().getBlob(context.getBlobIdent(), true).getText() != null) {
			return new PrioritizedComponentRenderer() {
				
				private static final long serialVersionUID = 1L;

				@Override
				public Component render(String componentId) {
					return new SourceViewPanel(componentId, context, false);
				}
				
				@Override
				public int getPriority() {
					return Integer.MAX_VALUE-1;
				}
			};
		} else {
			return null;
		}		
	}

}
