package io.onedev.server.web.component.user.avatar;

import org.apache.wicket.request.resource.CssResourceReference;

public class UserAvatarResourceReference extends CssResourceReference {

	private static final long serialVersionUID = 1L;

	public UserAvatarResourceReference() {
		super(UserAvatarResourceReference.class, "user-avatar.css");
	}

}
