package com.pmease.gitop.web.assets;

import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

public class AssetLocator {

	public static final ResourceReference BOOTSTRAP_CSS = newCssResourceReference("css/bootstrap.css");
	public static final ResourceReference BOOTSTRAP_MIN_CSS = newCssResourceReference("css/bootstrap.min.css");
	public static final ResourceReference FONT_AWESOME_CSS = newCssResourceReference("css/font-awesome.css");
	public static final ResourceReference FONT_AWESOME_MIN_CSS = newCssResourceReference("css/font-awesome.min.css");
	
	public static final ResourceReference BASE_CSS = newCssResourceReference("css/base.css");
	public static final ResourceReference PAGE_CSS = newCssResourceReference("css/page.css");
	
	public static final ResourceReference MODERNIZR_JS = newJavaScriptResourceReference("js/vendor/modernizr-2.6.2.js");
	public static final ResourceReference PAGE_JS = newJavaScriptResourceReference("js/page.js");
	public static final ResourceReference JQUERY_JS = newJavaScriptResourceReference("js/vendor/jquery.min.js");
	public static final ResourceReference JQUERY_UI_WIDGET_JS = newJavaScriptResourceReference("js/vendor/jquery.ui.widget.js");
	public static final ResourceReference BOOTSTAP_JS = newJavaScriptResourceReference("js/vendor/bootstrap.js");
	
	private static ResourceReference newCssResourceReference(String url) {
		return new CssResourceReference(AssetLocator.class, url);
	}
	
	private static ResourceReference newJavaScriptResourceReference(String url) {
		return new JavaScriptResourceReference(AssetLocator.class, url);
	}
}
