package com.pmease.commons.wicket;

import org.apache.wicket.Page;
import org.apache.wicket.core.request.handler.IPageRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;

import com.pmease.commons.wicket.websocket.PageKey;

public class WicketUtils {
	
	public static String relativizeUrl(String url) {
		if (Url.parse(url).isFull())
			return url;
		else
			return RequestCycle.get().getUrlRenderer().renderContextRelativeUrl(url);
	}
	
	public static PageKey getPageKey() {
		if (RequestCycle.get() != null && RequestCycle.get().getActiveRequestHandler() instanceof IPageRequestHandler) {
			IPageRequestHandler handler = (IPageRequestHandler) RequestCycle.get().getActiveRequestHandler();					
			return new PageKey((Page) handler.getPage());
		} else {
			return null;
		}
	}
	
}
