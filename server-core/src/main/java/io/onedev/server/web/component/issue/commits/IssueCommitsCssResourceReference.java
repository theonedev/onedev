package io.onedev.server.web.component.issue.commits;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class IssueCommitsCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public IssueCommitsCssResourceReference() {
		super(IssueCommitsCssResourceReference.class, "issue-commits.css");
	}

}
