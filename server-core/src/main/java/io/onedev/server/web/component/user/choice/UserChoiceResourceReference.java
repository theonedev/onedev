package io.onedev.server.web.component.user.choice;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class UserChoiceResourceReference extends BaseDependentResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public UserChoiceResourceReference() {
		super(UserChoiceResourceReference.class, "user-choice.js");
	}

}
