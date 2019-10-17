package io.onedev.server.web.page.project.blob.render.renderers.source;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

import io.onedev.commons.utils.PlanarRange;
import io.onedev.server.web.PrioritizedComponentRenderer;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext.Mode;
import io.onedev.server.web.page.project.blob.render.BlobRendererContribution;

public class SourceRendererProvider implements BlobRendererContribution {

	private static final long serialVersionUID = 1L;

	private static final String POSITION_PREFIX = "source-";
	
	@Nullable
	public static PlanarRange getRange(@Nullable String position) {
		if (position != null && position.startsWith(POSITION_PREFIX))
			return PlanarRange.of(position.substring(POSITION_PREFIX.length()));
		else
			return null;
	}
	
	@Nullable
	public static String getPosition(@Nullable PlanarRange range) {
		return range!=null? POSITION_PREFIX + range.toString(): null;
	}
	
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
