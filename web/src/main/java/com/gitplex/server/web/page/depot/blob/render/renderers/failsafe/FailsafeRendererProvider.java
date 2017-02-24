package com.gitplex.server.web.page.depot.blob.render.renderers.failsafe;

import org.apache.wicket.Component;

import com.gitplex.server.web.PrioritizedComponentRenderer;
import com.gitplex.server.web.page.depot.blob.render.BlobRenderContext;
import com.gitplex.server.web.page.depot.blob.render.BlobRendererContribution;

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
