package com.pmease.gitop.web.common.wicket.component.foundation;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class FoundationResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public FoundationResourceReference() {
		super(FoundationResourceReference.class, "res/js/foundation.js");
	}

	private static final FoundationResourceReference instance =
			new FoundationResourceReference();
	
	public static FoundationResourceReference get() {
		return instance;
	}
}
