package io.onedev.server.web.asset.marked;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class MarkedResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public MarkedResourceReference() {
		super(MarkedResourceReference.class, "marked.min.js");
	}

}
