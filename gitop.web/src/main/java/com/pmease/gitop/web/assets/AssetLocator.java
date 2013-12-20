package com.pmease.gitop.web.assets;

import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class AssetLocator {

//	public static final ResourceReference FONT_AWESOME_CSS = newCssResourceReference("css/font-awesome.css");
//	public static final ResourceReference FONT_AWESOME_MIN_CSS = newCssResourceReference("css/font-awesome.min.css");
	
	public static final CssResourceReference ICONS_CSS = newCssResourceReference("css/icons.css");
	
	public static final JavaScriptResourceReference MODERNIZR_JS = newJavaScriptResourceReference("js/vendor/modernizr-2.6.2.js");
	public static final JavaScriptResourceReference JQUERY_UI_WIDGET_JS = newJavaScriptResourceReference("js/vendor/jquery.ui.widget.js");

	public static final CssResourceReference BASE_CSS = newCssResourceReference("css/base.css");
	public static final CssResourceReference PAGE_CSS = newCssResourceReference("css/page.css");
	public static final JavaScriptResourceReference PAGE_JS = newJavaScriptResourceReference("js/page.js");

	private static CssResourceReference newCssResourceReference(String url) {
		return new CssResourceReference(AssetLocator.class, url);
	}
	
	private static JavaScriptResourceReference newJavaScriptResourceReference(String url) {
		return new JavaScriptResourceReference(AssetLocator.class, url);
	}
}
