package io.onedev.server.web.component.issue.choice;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class IssueChoiceResourceReference extends BaseDependentResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public IssueChoiceResourceReference() {
		super(IssueChoiceResourceReference.class, "issue-choice.js");
	}

}
