package com.gitplex.server.web.page.project.blob.render.renderers.folder;

import org.apache.wicket.Component;

import com.gitplex.server.web.PrioritizedComponentRenderer;
import com.gitplex.server.web.page.project.blob.render.BlobRenderContext;
import com.gitplex.server.web.page.project.blob.render.BlobRendererContribution;
import com.gitplex.server.web.page.project.blob.render.BlobRenderContext.Mode;

public class FolderViewRendererProvider implements BlobRendererContribution {

	private static final long serialVersionUID = 1L;

	@Override
	public PrioritizedComponentRenderer getRenderer(BlobRenderContext context) {
		if (context.getMode() == Mode.VIEW && context.getBlobIdent().isTree()) {
			return new PrioritizedComponentRenderer() {
				
				private static final long serialVersionUID = 1L;

				@Override
				public Component render(String componentId) {
					return new FolderViewPanel(componentId, context);
				}

				@Override
				public int getPriority() {
					return 0;
				}
				
			};
		} else {
			return null;
		}
	}

}
