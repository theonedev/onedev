package com.turbodev.server.web.page.layout;

import com.turbodev.server.web.page.base.BaseDependentCssResourceReference;

public class LayoutResourceReference extends BaseDependentCssResourceReference {

	private static final long serialVersionUID = 1L;

	public LayoutResourceReference() {
		super(LayoutResourceReference.class, "layout.css");
	}

}
