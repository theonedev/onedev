package com.pmease.commons.wicket.assets.d3;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class D3ResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static final D3ResourceReference INSTANCE = new D3ResourceReference();
	
	private D3ResourceReference() {
		super(D3ResourceReference.class, "d3.min.js");
	}

}
