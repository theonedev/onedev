package io.onedev.server.web.editable.refreshtoken;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class RefreshTokenResourceReference extends BaseDependentResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public RefreshTokenResourceReference() {
		super(RefreshTokenResourceReference.class, "refresh-token.js");
	}

}
