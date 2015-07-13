package com.pmease.gitplex.web.component.avatar;

import org.apache.wicket.request.resource.CssResourceReference;

public class AvatarResourceReference extends CssResourceReference {

	private static final long serialVersionUID = 1L;

	public static final AvatarResourceReference INSTANCE = new AvatarResourceReference();
	
	private AvatarResourceReference() {
		super(AvatarResourceReference.class, "avatar.css");
	}

}
