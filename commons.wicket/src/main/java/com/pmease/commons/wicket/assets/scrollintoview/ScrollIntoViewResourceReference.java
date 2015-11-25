package com.pmease.commons.wicket.assets.scrollintoview;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class ScrollIntoViewResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static final ScrollIntoViewResourceReference INSTANCE = new ScrollIntoViewResourceReference();
	
	private ScrollIntoViewResourceReference() {
		super(ScrollIntoViewResourceReference.class, "jquery.scrollintoview.js");
	}

}
