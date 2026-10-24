package io.onedev.server.web.component.build.choice;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class BuildChoiceResourceReference extends BaseDependentResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public BuildChoiceResourceReference() {
		super(BuildChoiceResourceReference.class, "build-choice.js");
	}

}
