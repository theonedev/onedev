package io.onedev.server.web.component.build.status;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class BuildStatusCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public BuildStatusCssResourceReference() {
		super(BuildStatusCssResourceReference.class, "build-status.css");
	}

}
