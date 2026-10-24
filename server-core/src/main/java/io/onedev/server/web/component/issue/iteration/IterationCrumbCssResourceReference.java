package io.onedev.server.web.component.issue.iteration;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class IterationCrumbCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public IterationCrumbCssResourceReference() {
		super(IterationCrumbCssResourceReference.class, "iteration-crumb.css");
	}

}
