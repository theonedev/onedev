package io.onedev.server.web.asset.lozad;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class LozadResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public LozadResourceReference() {
		super(LozadResourceReference.class, "lozad.min.js");
	}

}
