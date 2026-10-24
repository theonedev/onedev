package io.onedev.server.web.component.issue.list;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class IssueListCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public IssueListCssResourceReference() {
		super(IssueListCssResourceReference.class, "issue-list.css");
	}

}
