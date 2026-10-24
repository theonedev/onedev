package io.onedev.server.web.component.iteration.list;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class IterationListCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public IterationListCssResourceReference() {
		super(IterationListCssResourceReference.class, "iteration-list.css");
	}
}