package io.onedev.server.web.component.groupchoice;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class GroupChoiceResourceReference extends BaseDependentResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public GroupChoiceResourceReference() {
		super(GroupChoiceResourceReference.class, "group-choice.js");
	}

}
