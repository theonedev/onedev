package io.onedev.server.web.page.project.blob.render.renderers.markdown;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.Component;

import io.onedev.server.util.ProgrammingLanguageDetector;
import io.onedev.server.util.FileExtension;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext.Mode;
import io.onedev.server.web.page.project.blob.render.source.SourceViewPanel;
import io.onedev.server.web.page.project.blob.render.BlobRenderer;

public class MarkdownRenderer implements BlobRenderer {

	private boolean isMarkdown(@Nullable String blobPath) {
		String language = ProgrammingLanguageDetector.getLanguageForExtension(FileExtension.getExtension(blobPath));
		
		return blobPath != null && language != null && language.equals("Markdown");
	}
	
	@Override
	public Component render(String componentId, BlobRenderContext context) {
		if (context.getMode() == Mode.ADD && isMarkdown(context.getNewPath()) 
				|| context.getMode() == Mode.EDIT 
					&& isMarkdown(context.getBlobIdent().path) 
					&& context.getProject().getBlob(context.getBlobIdent(), true).getText() != null) {
			return new MarkdownBlobEditPanel(componentId, context);
		} else if ((context.getMode() == Mode.VIEW || context.getMode() == Mode.BLAME) 
				&& context.getBlobIdent().isFile() 
				&& isMarkdown(context.getBlobIdent().path) 
				&& context.getProject().getBlob(context.getBlobIdent(), true).getText() != null) {
			if (context.getPosition() != null || context.getMode() == Mode.BLAME) 
				return new SourceViewPanel(componentId, context, false);
			else
				return new MarkdownBlobViewPanel(componentId, context);
		} else {
			return null;
		}
	}

}
