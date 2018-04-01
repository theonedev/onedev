package io.onedev.server.web.page.project.blob.render.renderers.failsafe;

import org.apache.wicket.Component;

import io.onedev.server.web.PrioritizedComponentRenderer;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.BlobRendererContribution;

public class FailsafeRendererProvider implements BlobRendererContribution {

	private static final long serialVersionUID = 1L;

	@Override
	public PrioritizedComponentRenderer getRenderer(BlobRenderContext context) {
		return new PrioritizedComponentRenderer() {

			private static final long serialVersionUID = 1L;

			@Override
			public Component render(String componentId) {
				return new FailsafeViewPanel(componentId, context);
			}

			@Override
			public int getPriority() {
				return Integer.MAX_VALUE;
			}
			
		};
	}

}
