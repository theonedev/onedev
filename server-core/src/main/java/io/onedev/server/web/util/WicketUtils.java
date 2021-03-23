package io.onedev.server.web.util;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPageRequestHandler;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.protocol.ws.api.registry.PageIdKey;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestHandlerDelegate;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.web.websocket.PageKey;

public class WicketUtils {
	
	public static String relativizeUrl(String url) {
		if (Url.parse(url).isFull())
			return url;
		else
			return RequestCycle.get().getUrlRenderer().renderContextRelativeUrl(url);
	}
	
	@Nullable
	public static Page getPage() {
		RequestCycle requestCycle = RequestCycle.get();
		if (requestCycle != null) {
			IRequestHandler requestHandler = requestCycle.getActiveRequestHandler();
			if (requestHandler instanceof IRequestHandlerDelegate) 
				requestHandler = ((IRequestHandlerDelegate) requestHandler).getDelegateHandler();			
			if (requestHandler instanceof IPageRequestHandler)
				return (Page) ((IPageRequestHandler) requestHandler).getPage();	
		} 
		return null;
	}
	
	@Nullable
	public static <T> T findOutermost(Component component, Class<T> clazz) {
		List<T> parents = findParents(component, clazz);
		if (!parents.isEmpty())
			return parents.get(parents.size()-1);
		else
			return null;
	}
	
	@Nullable
	public static <T> T findInnermost(Component component, Class<T> clazz) {
		List<T> parents = findParents(component, clazz);
		if (!parents.isEmpty())
			return parents.get(0);
		else
			return null;
	}
	
	/**
	 * Get list of parent components (including current component) of specified clazz
	 * @param component
	 * 			starting component
	 * @param clazz
	 * 			clazz to check
	 * @return
	 * 			list of parent components of specified clazz, with inner component comes first
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> findParents(Component component, Class<T> clazz) {
		List<T> parents = new ArrayList<>();
		Component current = component;
		do {
			if (clazz.isAssignableFrom(current.getClass()))
				parents.add((T) current);
			current = current.getParent();
		} while (current != null);

		return parents;
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
	
	public static int getChildIndex(WebMarkupContainer parent, Component child) {
		int index = 0;
		for (Component each: parent) {
			if (each == child)
				return index;
			index++;
		}
		return -1;
	}
	
	public static class LastVisibleAppender extends AttributeAppender {

		private static final long serialVersionUID = 1L;

		public LastVisibleAppender(String attribute, String value) {
			super(attribute, value);
		}
		
	}
}
