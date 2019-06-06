package io.onedev.server.web.page.project.blob.render.renderers.cispec;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

import io.onedev.server.ci.CISpec;
import io.onedev.server.web.PrioritizedComponentRenderer;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext.Mode;
import io.onedev.server.web.page.project.blob.render.BlobRendererContribution;
import io.onedev.server.web.page.project.blob.render.renderers.source.SourceEditPanel;
import io.onedev.server.web.page.project.blob.render.renderers.source.SourceRendererProvider;
import io.onedev.server.web.page.project.blob.render.renderers.source.SourceViewPanel;

public class CISpecRendererProvider implements BlobRendererContribution {

	private static final long serialVersionUID = 1L;

	private static final String POSITION_PREFIX = "cispec-";
	
	private boolean isCISpec(@Nullable String blobPath) {
		return CISpec.BLOB_PATH.equals(blobPath);
	}
	
	@Nullable
	public static String getPosition(@Nullable String selection) {
		return selection!=null? POSITION_PREFIX + selection: null;
	}
	
	@Nullable
	public static String getSelection(@Nullable String position) {
		if (position != null && position.startsWith(POSITION_PREFIX))
			return position.substring(POSITION_PREFIX.length());
		else
			return null;
	}
	
	@Override
	public PrioritizedComponentRenderer getRenderer(BlobRenderContext context) {
		if (context.getMode() == Mode.ADD && isCISpec(context.getNewPath()) 
				|| context.getMode() == Mode.EDIT && isCISpec(context.getBlobIdent().path)) {
			return new PrioritizedComponentRenderer() {
				
				private static final long serialVersionUID = 1L;

				@Override
				public Component render(String componentId) {
					if (SourceRendererProvider.getRange(context.getPosition()) != null)
						return new SourceEditPanel(componentId, context);
					else
						return new CISpecBlobEditPanel(componentId, context);
				}
				
				@Override
				public int getPriority() {
					return 0;
				}
			};
		} else if ((context.getMode() == Mode.VIEW || context.getMode() == Mode.VIEW_PLAIN || context.getMode() == Mode.BLAME) 
				&& context.getBlobIdent().isFile() 
				&& isCISpec(context.getBlobIdent().path)) {
			return new PrioritizedComponentRenderer() {
				
				private static final long serialVersionUID = 1L;

				@Override
				public Component render(String componentId) {
					if (SourceRendererProvider.getRange(context.getPosition()) != null || context.getMode() == Mode.BLAME)
						return new SourceViewPanel(componentId, context, false);
					else if (context.getMode() == Mode.VIEW_PLAIN) 
						return new SourceViewPanel(componentId, context, true);
					else
						return new CISpecBlobViewPanel(componentId, context);
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
