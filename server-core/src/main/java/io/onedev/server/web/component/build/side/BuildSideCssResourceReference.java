package io.onedev.server.web.component.build.side;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class BuildSideCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public BuildSideCssResourceReference() {
		super(BuildSideCssResourceReference.class, "build-side.css");
	}

}
