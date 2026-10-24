package io.onedev.server.web.component.user.gpgkey;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class GpgKeyCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public GpgKeyCssResourceReference() {
		super(GpgKeyCssResourceReference.class, "gpg-key.css");
	}

}
