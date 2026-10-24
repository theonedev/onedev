package io.onedev.server.web.component.pack.side;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class PackSideCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public PackSideCssResourceReference() {
		super(PackSideCssResourceReference.class, "pack-side.css");
	}

}
