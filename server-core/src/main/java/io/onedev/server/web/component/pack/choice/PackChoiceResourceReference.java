package io.onedev.server.web.component.pack.choice;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class PackChoiceResourceReference extends BaseDependentResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public PackChoiceResourceReference() {
		super(PackChoiceResourceReference.class, "pack-choice.js");
	}

}
