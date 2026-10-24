package io.onedev.server.web.page.user;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class UserCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public UserCssResourceReference() {
		super(UserCssResourceReference.class, "user.css");
	}

}
