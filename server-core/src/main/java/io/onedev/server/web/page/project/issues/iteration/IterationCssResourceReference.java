package io.onedev.server.web.page.project.issues.iteration;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class IterationCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;
	
	public IterationCssResourceReference() {
		super(IterationCssResourceReference.class, "iteration.css");
	}

}
