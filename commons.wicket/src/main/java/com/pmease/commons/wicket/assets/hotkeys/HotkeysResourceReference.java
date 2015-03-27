package com.pmease.commons.wicket.assets.hotkeys;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class HotkeysResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static final HotkeysResourceReference INSTANCE = new HotkeysResourceReference();
	
	private HotkeysResourceReference() {
		super(HotkeysResourceReference.class, "jquery.hotkeys.js");
	}

}
