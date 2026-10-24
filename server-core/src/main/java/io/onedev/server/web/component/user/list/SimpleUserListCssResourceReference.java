package io.onedev.server.web.component.user.list;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class SimpleUserListCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public SimpleUserListCssResourceReference() {
		super(SimpleUserListCssResourceReference.class, "simple-user-list.css");
	}

}
