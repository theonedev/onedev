package io.onedev.server.web.page.simple;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class SimpleCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public SimpleCssResourceReference() {
		super(SimpleCssResourceReference.class, "simple.css");
	}

}
