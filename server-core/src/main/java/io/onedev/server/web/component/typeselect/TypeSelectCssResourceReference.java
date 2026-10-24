package io.onedev.server.web.component.typeselect;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class TypeSelectCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public TypeSelectCssResourceReference() {
		super(TypeSelectCssResourceReference.class, "type-select.css");
	}

}
