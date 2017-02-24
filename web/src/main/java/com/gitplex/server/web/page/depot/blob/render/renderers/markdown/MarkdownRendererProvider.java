package com.gitplex.server.web.page.depot.blob.render.renderers.markdown;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

import com.gitplex.server.web.PrioritizedComponentRenderer;
import com.gitplex.server.web.page.depot.blob.render.BlobRenderContext;
import com.gitplex.server.web.page.depot.blob.render.BlobRenderContext.Mode;
import com.gitplex.server.web.page.depot.blob.render.BlobRendererContribution;
import com.gitplex.server.web.page.depot.blob.render.renderers.source.SourceViewPanel;

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
					&& context.getDepot().getBlob(context.getBlobIdent()).getText() != null) {
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
				&& context.getDepot().getBlob(context.getBlobIdent()).getText() != null) {
			return new PrioritizedComponentRenderer() {
				
				private static final long serialVersionUID = 1L;

				@Override
				public Component render(String componentId) {
					if (context.getMark() != null || context.getMode() == Mode.BLAME) 
						return new SourceViewPanel(componentId, context);
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
