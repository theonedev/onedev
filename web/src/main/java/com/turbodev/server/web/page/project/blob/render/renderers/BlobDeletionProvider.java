package com.turbodev.server.web.page.project.blob.render.renderers;

import org.apache.wicket.Component;

import com.turbodev.server.web.PrioritizedComponentRenderer;
import com.turbodev.server.web.page.project.blob.render.BlobRenderContext;
import com.turbodev.server.web.page.project.blob.render.BlobRendererContribution;
import com.turbodev.server.web.page.project.blob.render.BlobRenderContext.Mode;
import com.turbodev.server.web.page.project.blob.render.commitoption.CommitOptionPanel;

public class BlobDeletionProvider implements BlobRendererContribution {

	private static final long serialVersionUID = 1L;

	@Override
	public PrioritizedComponentRenderer getRenderer(BlobRenderContext context) {
		if (context.getMode() == Mode.DELETE) {
			
			return new PrioritizedComponentRenderer() {
				
				private static final long serialVersionUID = 1L;

				@Override
				public Component render(String componentId) {
					return new CommitOptionPanel(componentId, context, null);
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
