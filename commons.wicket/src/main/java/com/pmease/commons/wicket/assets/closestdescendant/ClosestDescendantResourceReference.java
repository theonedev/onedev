package com.pmease.commons.wicket.assets.closestdescendant;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class ClosestDescendantResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static final ClosestDescendantResourceReference INSTANCE = new ClosestDescendantResourceReference();
	
	private ClosestDescendantResourceReference() {
		super(ClosestDescendantResourceReference.class, "closest-descendant.js");
	}

}
