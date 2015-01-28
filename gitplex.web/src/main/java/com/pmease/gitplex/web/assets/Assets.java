package com.pmease.gitplex.web.assets;

import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class Assets {

	public static final CssResourceReference PONTICONS_CSS = newCssResourceReference("ponticons/css/ponticons.css");
	
	public static final JavaScriptResourceReference LIVEFILTER_JS = newJavaScriptResourceReference("js/jquery.livefilter.js");
	
	public static final JavaScriptResourceReference PAGE_JS = newJavaScriptResourceReference("js/page.js");

	public static final CssResourceReference PAGE_CSS = newCssResourceReference("css/page.css");

	private static CssResourceReference newCssResourceReference(String url) {
		return new CssResourceReference(Assets.class, url);
	}
	
	private static JavaScriptResourceReference newJavaScriptResourceReference(String url) {
		return new JavaScriptResourceReference(Assets.class, url);
	}
}
