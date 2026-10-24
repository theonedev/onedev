package io.onedev.server.web.asset.cookies;

import java.io.Serial;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class CookiesResourceReference extends JavaScriptResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public CookiesResourceReference() {
		super(CookiesResourceReference.class, "cookies.min.js");
	}

}
