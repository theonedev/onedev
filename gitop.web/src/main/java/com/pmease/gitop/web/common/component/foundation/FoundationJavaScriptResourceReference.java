package com.pmease.gitop.web.common.component.foundation;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class FoundationJavaScriptResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public FoundationJavaScriptResourceReference() {
		super(FoundationJavaScriptResourceReference.class,
				"res/js/foundation.js");
	}

	private static final FoundationJavaScriptResourceReference instance =
			new FoundationJavaScriptResourceReference();
	
	public static FoundationJavaScriptResourceReference get() {
		return instance;
	}
}
