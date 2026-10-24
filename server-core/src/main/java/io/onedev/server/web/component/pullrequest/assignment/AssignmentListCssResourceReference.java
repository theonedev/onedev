package io.onedev.server.web.component.pullrequest.assignment;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class AssignmentListCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public AssignmentListCssResourceReference() {
		super(AssignmentListCssResourceReference.class, "assignment-list.css");
	}

}
