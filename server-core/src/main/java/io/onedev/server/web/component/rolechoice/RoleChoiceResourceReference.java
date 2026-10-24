package io.onedev.server.web.component.rolechoice;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class RoleChoiceResourceReference extends BaseDependentResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public RoleChoiceResourceReference() {
		super(RoleChoiceResourceReference.class, "role-choice.js");
	}

}
