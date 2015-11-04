package com.pmease.commons.wicket.assets.align;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class AlignResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static final AlignResourceReference INSTANCE = new AlignResourceReference();
	
	private AlignResourceReference() {
		super(AlignResourceReference.class, "jquery.align.js");
	}

}
