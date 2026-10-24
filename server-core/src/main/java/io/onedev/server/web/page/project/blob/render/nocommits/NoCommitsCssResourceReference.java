package io.onedev.server.web.page.project.blob.render.nocommits;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class NoCommitsCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public NoCommitsCssResourceReference() {
		super(NoCommitsCssResourceReference.class, "no-commits.css");
	}

}
