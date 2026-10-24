package io.onedev.server.web.component.branch.choice;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class BranchChoiceResourceReference extends BaseDependentResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public BranchChoiceResourceReference() {
		super(BranchChoiceResourceReference.class, "branch-choice.js");
	}

}
