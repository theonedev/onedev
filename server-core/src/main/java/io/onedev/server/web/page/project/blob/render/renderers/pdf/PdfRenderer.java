package io.onedev.server.web.page.project.blob.render.renderers.pdf;

import org.apache.tika.mime.MediaType;
import org.apache.wicket.Component;

import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext.Mode;
import io.onedev.server.web.page.project.blob.render.BlobRenderer;

public class PdfRenderer implements BlobRenderer {

	private static final long serialVersionUID = 1L;

	@Override
	public Component render(String componentId, BlobRenderContext context) {
		MediaType mediaType = context.getProject().detectMediaType(context.getBlobIdent());
		if (context.getMode() == Mode.VIEW && isPdf(mediaType)) { 
			return new PdfViewPanel(componentId, context);
		} else { 
			return null;
		}
	}

	private boolean isPdf(MediaType mediaType) {
		return mediaType != null
				&& mediaType.getType().equalsIgnoreCase("application")
				&& (mediaType.getSubtype().equalsIgnoreCase("pdf") || mediaType.getSubtype().equalsIgnoreCase("x-pdf"));
	}

}
