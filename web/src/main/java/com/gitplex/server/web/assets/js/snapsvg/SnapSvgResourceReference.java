package com.gitplex.server.web.assets.js.snapsvg;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class SnapSvgResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;
	
	public SnapSvgResourceReference() {
		super(SnapSvgResourceReference.class, "snap.svg-min.js");
	}

}
