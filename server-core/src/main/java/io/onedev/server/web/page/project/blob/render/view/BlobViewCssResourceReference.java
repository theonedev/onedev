package io.onedev.server.web.page.project.blob.render.view;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class BlobViewCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public BlobViewCssResourceReference() {
		super(BlobViewCssResourceReference.class, "blob-view.css");
	}

}
