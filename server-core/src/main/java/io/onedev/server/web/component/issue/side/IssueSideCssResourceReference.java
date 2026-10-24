package io.onedev.server.web.component.issue.side;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class IssueSideCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public IssueSideCssResourceReference() {
		super(IssueSideCssResourceReference.class, "issue-side.css");
	}

}
