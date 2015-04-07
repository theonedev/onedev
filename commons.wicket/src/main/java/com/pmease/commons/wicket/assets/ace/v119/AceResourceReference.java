package com.pmease.commons.wicket.assets.ace.v119;

import com.pmease.commons.wicket.VersionlessJavaScriptResourceReference;

public class AceResourceReference extends VersionlessJavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static final AceResourceReference INSTANCE = new AceResourceReference();
	
	private AceResourceReference() {
		super(AceResourceReference.class, "src-noconflict/ace.js");
	}

}
