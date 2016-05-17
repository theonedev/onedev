package com.pmease.commons.wicket.assets.jqueryui;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

/*
 * Do not include jquery ui css here as otherwise the search result resizing does not work
 */
public class JQueryUIResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static final JQueryUIResourceReference INSTANCE = new JQueryUIResourceReference();
	
	private JQueryUIResourceReference() {
		super(JQueryUIResourceReference.class, "jquery-ui.min.js");
	}

}
