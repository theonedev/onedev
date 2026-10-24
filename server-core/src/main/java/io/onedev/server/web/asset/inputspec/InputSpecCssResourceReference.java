package io.onedev.server.web.asset.inputspec;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class InputSpecCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public InputSpecCssResourceReference() {
		super(InputSpecCssResourceReference.class, "input-spec.css");
	}

}
