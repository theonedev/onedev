package com.pmease.commons.wicket.assets.oneline;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class OnelineResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static final OnelineResourceReference INSTANCE = new OnelineResourceReference();
	
	private OnelineResourceReference() {
		super(OnelineResourceReference.class, "jquery.oneline.js");
	}

}
