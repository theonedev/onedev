package io.onedev.server.web.behavior.dropdown;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class DropdownHoverResourceReference extends BaseDependentResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public DropdownHoverResourceReference() {
		super(DropdownHoverResourceReference.class, "dropdown-hover.js");
	}

}
