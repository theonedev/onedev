package com.pmease.gitop.web.assets;

import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class AssetLocator {

	public static final CssResourceReference ICONS_CSS = newCssResourceReference("css/icons.css");
	
	public static final JavaScriptResourceReference PAGE_JS = newJavaScriptResourceReference("js/page.js");

	public static final CssResourceReference PAGE_CSS = newCssResourceReference("css/page.css");

	public static final JavaScriptResourceReference ARE_YOU_SURE_JS = newJavaScriptResourceReference("js/vendor/jquery.are-you-sure.js");
	
	private static CssResourceReference newCssResourceReference(String url) {
		return new CssResourceReference(AssetLocator.class, url);
	}
	
	private static JavaScriptResourceReference newJavaScriptResourceReference(String url) {
		return new JavaScriptResourceReference(AssetLocator.class, url);
	}
}
