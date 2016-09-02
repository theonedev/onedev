package com.pmease.gitplex.web.page.layout;

import org.apache.wicket.request.resource.CssResourceReference;

public class LayoutResourceReference extends CssResourceReference {

	private static final long serialVersionUID = 1L;

	public LayoutResourceReference() {
		super(LayoutResourceReference.class, "layout.css");
	}

}
