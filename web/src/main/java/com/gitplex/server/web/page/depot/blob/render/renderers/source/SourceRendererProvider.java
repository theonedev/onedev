package com.gitplex.server.web.page.depot.blob.render.renderers.source;

import org.apache.wicket.Component;

import com.gitplex.server.web.PrioritizedComponentRenderer;
import com.gitplex.server.web.page.depot.blob.render.BlobRenderContext;
import com.gitplex.server.web.page.depot.blob.render.BlobRenderContext.Mode;
import com.gitplex.server.web.page.depot.blob.render.BlobRendererContribution;

public class SourceRendererProvider implements BlobRendererContribution {

	private static final long serialVersionUID = 1L;

	@Override
	public PrioritizedComponentRenderer getRenderer(BlobRenderContext context) {
		if (context.getMode() == Mode.ADD 
				|| context.getMode() == Mode.EDIT 
						&& context.getDepot().getBlob(context.getBlobIdent()).getText() != null) {
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
				&& context.getDepot().getBlob(context.getBlobIdent()).getText() != null) {
			return new PrioritizedComponentRenderer() {
				
				private static final long serialVersionUID = 1L;

				@Override
				public Component render(String componentId) {
					return new SourceViewPanel(componentId, context);
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
