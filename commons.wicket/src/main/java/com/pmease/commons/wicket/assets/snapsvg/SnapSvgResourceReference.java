package com.pmease.commons.wicket.assets.snapsvg;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class SnapSvgResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static final SnapSvgResourceReference INSTANCE = new SnapSvgResourceReference();
	
	private SnapSvgResourceReference() {
		super(SnapSvgResourceReference.class, "snap.svg-min.js");
	}

}
