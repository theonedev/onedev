package com.turbodev.server.web.util;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.core.request.handler.IPageRequestHandler;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.protocol.ws.api.registry.PageIdKey;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;

import com.turbodev.server.web.websocket.PageKey;

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
		if (page != null) {
			String sessionId = page.getSession().getId();
			if (sessionId != null) {
				return new PageKey(sessionId, new PageIdKey(page.getPageId()));
			}
		}
		return null;
	}
	
	public static boolean isDevice() {
		HttpServletRequest request = (HttpServletRequest) RequestCycle.get().getRequest().getContainerRequest();
		String userAgent = request.getHeader("User-Agent").toLowerCase();
		return userAgent.indexOf("android") != -1 
				|| userAgent.indexOf("iphone") != -1 
				|| userAgent.indexOf("ipad") != -1 
				|| userAgent.indexOf("windows phone") != -1; 
	}
	
	public static void markLastVisibleChild(WebMarkupContainer container) {
		Component lastVisible = null;
		for (Component child: container) {
			for (Behavior behavior: child.getBehaviors()) {
				if (behavior instanceof LastVisibleAppender) {
					child.remove(behavior);
				}
			}
			child.configure();
			if (child.isVisible())
				lastVisible = child;
		}
		if (lastVisible != null)
			lastVisible.add(new LastVisibleAppender("class", "last-visible").setSeparator(" "));
	}

	public static class LastVisibleAppender extends AttributeAppender {

		private static final long serialVersionUID = 1L;

		public LastVisibleAppender(String attribute, String value) {
			super(attribute, value);
		}
		
	}
}
