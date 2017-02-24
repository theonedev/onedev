package com.gitplex.server.web.page.depot.blob.render.renderers.image;

import org.apache.wicket.request.resource.CssResourceReference;

public class ImageViewResourceReference extends CssResourceReference {

	private static final long serialVersionUID = 1L;

	public ImageViewResourceReference() {
		super(ImageViewResourceReference.class, "image-view.css");
	}

}
