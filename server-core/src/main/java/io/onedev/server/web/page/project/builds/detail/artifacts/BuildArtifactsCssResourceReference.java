package io.onedev.server.web.page.project.builds.detail.artifacts;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class BuildArtifactsCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public BuildArtifactsCssResourceReference() {
		super(BuildArtifactsCssResourceReference.class, "build-artifacts.css");
	}

}
