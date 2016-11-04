package com.gitplex.server.web.page.init;

import org.apache.wicket.request.resource.CssResourceReference;

public class WelcomeResourceReference extends CssResourceReference {

	private static final long serialVersionUID = 1L;

	public WelcomeResourceReference() {
		super(WelcomeResourceReference.class, "welcome.css");
	}

}
