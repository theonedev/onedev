package io.onedev.server.web.page.project.blob.render.renderers.video;

import org.apache.wicket.Component;

import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext.Mode;
import io.onedev.server.web.page.project.blob.render.BlobRenderer;

public class VideoRenderer implements BlobRenderer {

	private static final long serialVersionUID = 1L;

	@Override
	public Component render(String componentId, BlobRenderContext context) {
		if (context.getMode() == Mode.VIEW 
				&& context.getProject().detectMediaType(context.getBlobIdent()).getType().equalsIgnoreCase("video")) { 
			return new VideoViewPanel(componentId, context);
		} else {
			return null;
		}
	}

}
