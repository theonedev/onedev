package io.onedev.server.web.asset.marked;

import java.io.Serial;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class MarkedResourceReference extends JavaScriptResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public MarkedResourceReference() {
		super(MarkedResourceReference.class, "marked.min.js");
	}

}
