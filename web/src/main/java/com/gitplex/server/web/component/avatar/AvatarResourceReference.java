package com.gitplex.server.web.component.avatar;

import org.apache.wicket.request.resource.CssResourceReference;

public class AvatarResourceReference extends CssResourceReference {

	private static final long serialVersionUID = 1L;

	public AvatarResourceReference() {
		super(AvatarResourceReference.class, "avatar.css");
	}

}
