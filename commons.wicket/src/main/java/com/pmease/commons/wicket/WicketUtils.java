package com.pmease.commons.wicket;

import org.apache.wicket.core.request.handler.IPageRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.cycle.RequestCycle;

public class WicketUtils {
	
	public static String relativizeUrl(String url) {
		if (Url.parse(url).isFull())
			return url;
		else
			return RequestCycle.get().getUrlRenderer().renderContextRelativeUrl(url);
	}
	
	public static IRequestablePage getPage() {
		if (RequestCycle.get() != null && RequestCycle.get().getActiveRequestHandler() instanceof IPageRequestHandler) {
			IPageRequestHandler handler = (IPageRequestHandler) RequestCycle.get().getActiveRequestHandler();					
			return handler.getPage();
		} else {
			return null;
		}
	}
	
}
