package io.onedev.server.web.asset.jsjoda;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class JsJodaResourceReference extends BaseDependentResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public JsJodaResourceReference() {
		super(JsJodaResourceReference.class, "js-joda.min.js");
	}

}
