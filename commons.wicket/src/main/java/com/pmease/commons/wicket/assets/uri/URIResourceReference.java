package com.pmease.commons.wicket.assets.uri;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class URIResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public URIResourceReference() {
		super(URIResourceReference.class, "URI.js");
	}

}
