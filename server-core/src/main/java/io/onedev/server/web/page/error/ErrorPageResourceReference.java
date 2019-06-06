package io.onedev.server.web.page.error;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class ErrorPageResourceReference extends BaseDependentCssResourceReference {

	private static final long serialVersionUID = 1L;

	public ErrorPageResourceReference() {
		super(ErrorPageResourceReference.class, "error-page.css");
	}

}
