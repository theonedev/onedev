package com.pmease.commons.wicket.asset;

import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class Asset {
	
	public static final JavaScriptResourceReference ARE_YOU_SURE_JS = 
			new JavaScriptResourceReference(Asset.class, "jquery.are-you-sure.patched.js");
	
	public static final JavaScriptResourceReference COMMON_JS = 
			new JavaScriptResourceReference(Asset.class, "common.js");
	
	public static final CssResourceReference COMMON_CSS = 
			new CssResourceReference(Asset.class, "common.css");
	
}
