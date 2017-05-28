package com.gitplex.server.web.page.user;

import org.apache.wicket.request.resource.CssResourceReference;

public class UserResourceReference extends CssResourceReference {

	private static final long serialVersionUID = 1L;

	public UserResourceReference() {
		super(UserResourceReference.class, "user.css");
	}

}
