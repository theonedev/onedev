package io.onedev.server.web.asset.moment;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class MomentResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public MomentResourceReference() {
		super(MomentResourceReference.class, "moment.js");
	}
	
}
