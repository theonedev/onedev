package io.onedev.server.web.page.admin.gpgtrustedkeys;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class GpgTrustedKeysCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public GpgTrustedKeysCssResourceReference() {
		super(GpgTrustedKeysCssResourceReference.class, "gpg-trusted-keys.css");
	}

}
