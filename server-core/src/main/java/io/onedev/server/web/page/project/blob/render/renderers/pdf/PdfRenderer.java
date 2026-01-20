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
		if (context.getMode() == Mode.VIEW && context.getProject().detectMediaType(context.getBlobIdent()).equals(MediaType.application("pdf"))) { 
			return new PdfViewPanel(componentId, context);
		} else { 
			return null;
		}
	}

}
