package io.onedev.server.web.page.project.blob.render.renderers.markdown;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

import io.onedev.server.web.PrioritizedComponentRenderer;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.BlobRendererContribution;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext.Mode;
import io.onedev.server.web.page.project.blob.render.renderers.source.SourceViewPanel;

@SuppressWarnings("serial")
public class MarkdownRendererProvider implements BlobRendererContribution {

	private boolean isMarkdown(@Nullable String blobPath) {
		return blobPath != null && blobPath.endsWith(".md");
	}
	
	@Override
	public PrioritizedComponentRenderer getRenderer(BlobRenderContext context) {
		if (context.getMode() == Mode.ADD && isMarkdown(context.getNewPath()) 
				|| context.getMode() == Mode.EDIT 
					&& isMarkdown(context.getBlobIdent().path) 
					&& context.getProject().getBlob(context.getBlobIdent(), true).getText() != null) {
			return new PrioritizedComponentRenderer() {
				
				private static final long serialVersionUID = 1L;

				@Override
				public Component render(String componentId) {
					return new MarkdownBlobEditPanel(componentId, context);
				}
				
				@Override
				public int getPriority() {
					return 0;
				}
			};
		} else if ((context.getMode() == Mode.VIEW || context.getMode() == Mode.BLAME) 
				&& context.getBlobIdent().isFile() 
				&& isMarkdown(context.getBlobIdent().path) 
				&& context.getProject().getBlob(context.getBlobIdent(), true).getText() != null) {
			return new PrioritizedComponentRenderer() {
				
				private static final long serialVersionUID = 1L;

				@Override
				public Component render(String componentId) {
					if (context.getPosition() != null || context.getMode() == Mode.BLAME) 
						return new SourceViewPanel(componentId, context, false);
					else
						return new MarkdownBlobViewPanel(componentId, context);
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
