package io.onedev.server.web.page.project.blob.render.renderers.image;

import java.io.Serial;

import org.apache.wicket.request.resource.CssResourceReference;

public class ImageViewCssResourceReference extends CssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public ImageViewCssResourceReference() {
		super(ImageViewCssResourceReference.class, "image-view.css");
	}

}
