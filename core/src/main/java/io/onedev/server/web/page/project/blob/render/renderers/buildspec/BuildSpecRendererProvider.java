package io.onedev.server.web.page.project.blob.render.renderers.buildspec;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

import io.onedev.server.build.BuildSpec;
import io.onedev.server.web.PrioritizedComponentRenderer;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext.Mode;
import io.onedev.server.web.page.project.blob.render.BlobRendererContribution;
import io.onedev.server.web.page.project.blob.render.renderers.source.SourceViewPanel;

public class BuildSpecRendererProvider implements BlobRendererContribution {

	private static final long serialVersionUID = 1L;

	private boolean isBuildSpec(@Nullable String blobPath) {
		return BuildSpec.BLOB_PATH.equals(blobPath);
	}
	
	@Override
	public PrioritizedComponentRenderer getRenderer(BlobRenderContext context) {
		if (context.getMode() == Mode.ADD && isBuildSpec(context.getNewPath()) 
				|| context.getMode() == Mode.EDIT 
					&& isBuildSpec(context.getBlobIdent().path) 
					&& context.getProject().getBlob(context.getBlobIdent()).getText() != null) {
			return new PrioritizedComponentRenderer() {
				
				private static final long serialVersionUID = 1L;

				@Override
				public Component render(String componentId) {
					return new BuildSpecEditPanel(componentId, context);
				}
				
				@Override
				public int getPriority() {
					return 0;
				}
			};
		} else if ((context.getMode() == Mode.VIEW || context.getMode() == Mode.VIEW_PLAIN || context.getMode() == Mode.BLAME) 
				&& context.getBlobIdent().isFile() 
				&& isBuildSpec(context.getBlobIdent().path) 
				&& context.getProject().getBlob(context.getBlobIdent()).getText() != null) {
			return new PrioritizedComponentRenderer() {
				
				private static final long serialVersionUID = 1L;

				@Override
				public Component render(String componentId) {
					if (context.getMark() != null || context.getMode() == Mode.BLAME)
						return new SourceViewPanel(componentId, context, false);
					else if (context.getMode() == Mode.VIEW_PLAIN) 
						return new SourceViewPanel(componentId, context, true);
					else
						return new BuildSpecViewPanel(componentId, context);
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
