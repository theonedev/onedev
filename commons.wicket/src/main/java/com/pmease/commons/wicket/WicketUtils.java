package com.pmease.commons.wicket;

import javax.annotation.Nullable;

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
	
	@Nullable
	public static Page getPage() {
		if (RequestCycle.get() != null && RequestCycle.get().getActiveRequestHandler() instanceof IPageRequestHandler) {
			return (Page) ((IPageRequestHandler) RequestCycle.get().getActiveRequestHandler()).getPage();	
		} else {
			return null;
		}
	}

	@Nullable
	public static PageKey getPageKey() {
		Page page = getPage();
		if (page != null)
			return new PageKey(page);
		else
			return null;
	}
	
}
