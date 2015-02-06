package com.pmease.commons.wicket.assets;

import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class Assets {
	
	public static final JavaScriptResourceReference ARE_YOU_SURE_JS = 
			new JavaScriptResourceReference(Assets.class, "jquery.are-you-sure.patched.js");
	
	public static final JavaScriptResourceReference STICKY_JS = 
			new JavaScriptResourceReference(Assets.class, "jquery.sticky-kit.js");

	public static final JavaScriptResourceReference ALIGN_JS = 
			new JavaScriptResourceReference(Assets.class, "jquery.align.js");

	public static final JavaScriptResourceReference COMMON_JS = 
			new JavaScriptResourceReference(Assets.class, "common.js");
	
	public static final CssResourceReference COMMON_CSS = 
			new CssResourceReference(Assets.class, "common.css");
	
}
