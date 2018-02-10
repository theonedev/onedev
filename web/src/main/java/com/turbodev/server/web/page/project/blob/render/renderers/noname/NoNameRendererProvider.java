package com.turbodev.server.web.page.project.blob.render.renderers.noname;

import org.apache.wicket.Component;

import com.turbodev.server.web.PrioritizedComponentRenderer;
import com.turbodev.server.web.page.project.blob.render.BlobRenderContext;
import com.turbodev.server.web.page.project.blob.render.BlobRendererContribution;
import com.turbodev.server.web.page.project.blob.render.BlobRenderContext.Mode;

public class NoNameRendererProvider implements BlobRendererContribution {

	private static final long serialVersionUID = 1L;

	@Override
	public PrioritizedComponentRenderer getRenderer(BlobRenderContext context) {
		if (context.getMode() == Mode.ADD && context.getNewPath() == null) {
			return new PrioritizedComponentRenderer() {
				
				private static final long serialVersionUID = 1L;

				@Override
				public Component render(String componentId) {
					return new NoNameEditPanel(componentId, context);
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
