package io.onedev.server.web.page.project.blob.navigator;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class BlobNavigatorResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public BlobNavigatorResourceReference() {
		super(BlobNavigatorResourceReference.class, "blob-navigator.css");
	}

}
