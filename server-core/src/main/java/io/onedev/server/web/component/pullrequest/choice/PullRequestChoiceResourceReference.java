package io.onedev.server.web.component.pullrequest.choice;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class PullRequestChoiceResourceReference extends BaseDependentResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public PullRequestChoiceResourceReference() {
		super(PullRequestChoiceResourceReference.class, "pullrequest-choice.js");
	}

}
