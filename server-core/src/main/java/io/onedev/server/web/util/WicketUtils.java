package io.onedev.server.web.util;

import io.onedev.server.OneDev;
import io.onedev.server.SubscriptionManager;
import io.onedev.server.util.LongRange;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.websocket.PageKey;
import org.apache.wicket.Component;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPageRequestHandler;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.protocol.ws.api.registry.PageIdKey;
import org.apache.wicket.request.IRequestHandlerDelegate;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.AbstractResource;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

public class WicketUtils {

	private static class SubscriptionActiveKey extends MetaDataKey<Boolean> {
		static final SubscriptionActiveKey INSTANCE = new SubscriptionActiveKey();
	};
	
	@Nullable
	public static BasePage getPage() {
		var pageRequestHandler = getPageRequestHandler();
		if (pageRequestHandler != null)
			return (BasePage) pageRequestHandler.getPage();
		else 
			return null;
	}
	
	public static boolean isSubscriptionActive() {
		var requestCycle = RequestCycle.get();
		if (requestCycle == null)
			throw new IllegalStateException("No active request cycle");
		Boolean subscriptionActive = requestCycle.getMetaData(SubscriptionActiveKey.INSTANCE);
		if (subscriptionActive == null) {
			subscriptionActive = OneDev.getInstance(SubscriptionManager.class).isSubscriptionActive();
			requestCycle.setMetaData(SubscriptionActiveKey.INSTANCE, subscriptionActive);
		}
		return subscriptionActive;
	}

	@Nullable
	private static IPageRequestHandler getPageRequestHandler() {
		var requestCycle = RequestCycle.get();
		if (requestCycle != null) {
			var requestHandler = requestCycle.getActiveRequestHandler();
			if (requestHandler instanceof IRequestHandlerDelegate)
				requestHandler = ((IRequestHandlerDelegate) requestHandler).getDelegateHandler();
			if (requestHandler instanceof IPageRequestHandler)
				return ((IPageRequestHandler) requestHandler);
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
		var pageRequestHandler = getPageRequestHandler();
		if (pageRequestHandler != null) {
			var pageId = pageRequestHandler.getPageId();
			if (pageId != null) {
				var session = WebSession.get();
				return new PageKey(session.getId(), new PageIdKey(pageId));
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
	
	public static LongRange getRequestContentRange(long contentLength) {
		Long start = RequestCycle.get().getMetaData(AbstractResource.CONTENT_RANGE_STARTBYTE);
		Long end = RequestCycle.get().getMetaData(AbstractResource.CONTENT_RANGE_ENDBYTE);

		if (start == null)
			start = 0L;
		if (end == null || end == -1) 
			end = contentLength;
		
		return new LongRange(start, end);
	}
	
	public static class LastVisibleAppender extends AttributeAppender {

		private static final long serialVersionUID = 1L;

		public LastVisibleAppender(String attribute, String value) {
			super(attribute, value);
		}
		
	}
	
}
