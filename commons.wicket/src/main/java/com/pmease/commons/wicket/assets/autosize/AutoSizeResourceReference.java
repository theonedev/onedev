package com.pmease.commons.wicket.assets.autosize;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class AutoSizeResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static final AutoSizeResourceReference INSTANCE = new AutoSizeResourceReference();
	
	private AutoSizeResourceReference() {
		super(AutoSizeResourceReference.class, "autosize.js");
	}

}
