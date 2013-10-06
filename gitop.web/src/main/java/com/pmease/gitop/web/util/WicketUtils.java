package com.pmease.gitop.web.util;

import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.core.request.handler.IPageRequestHandler;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

public final class WicketUtils {
	private WicketUtils() {
	}

	public static Optional<Page> getCurrentPage() {
		Page page = null;
		if (RequestCycle.get().getActiveRequestHandler() instanceof IPageRequestHandler) {
			page = (Page) ((IPageRequestHandler) RequestCycle.get().getActiveRequestHandler())
					.getPage();
		}
		
		return Optional.fromNullable(page);
	}

	public static Optional<PageParameters> getCurrentPageParameters() {
		PageParameters params = null;
		Optional<Page> page = getCurrentPage();
		if (page.isPresent()) {
			params = page.get().getPageParameters();
		}
		
		return Optional.fromNullable(params);
	}
	
	public static boolean isCurrentRequestAjax() {
		return RequestCycle.get() != null
			? RequestCycle.get().find(AjaxRequestTarget.class) != null : false;
	}

	private static final String QUERY_STRING_SEPARATOR = "?";
	public static String getCurrentRequestUrl() {
		Request request = RequestCycle.get().getRequest();
		if (!(request instanceof ServletWebRequest)) {
			throw new IllegalStateException("Cannot be used in a non servlet environment");
		}
		
		ServletWebRequest servletWebRequest = (ServletWebRequest) request;
		StringBuffer currentUrl = servletWebRequest.getContainerRequest().getRequestURL();
		String queryString = servletWebRequest.getContainerRequest().getQueryString();
		if (queryString != null) {
			currentUrl.append(QUERY_STRING_SEPARATOR).append(queryString);
		}
		
		return currentUrl.toString();
	}
	
	public static void ajaxAdd(AjaxRequestTarget target, Component component) {
		if (target != null) {
			target.add(component);
		}
	}

	public static void appendJavaScript(AjaxRequestTarget target,
			CharSequence script) {
		if (target != null) {
			target.appendJavaScript(script);
		}
	}

	public static HttpServletRequest getHttpServletRequest() {
		WebRequest request = (WebRequest) RequestCycle.get().getRequest();
		return (HttpServletRequest) request.getContainerRequest();
	}

	public static HttpServletResponse getHttpServletResponse() {
		WebResponse response = (WebResponse) RequestCycle.get().getResponse();
		return (HttpServletResponse) response.getContainerResponse();
	}

	public static Cookie getCookie(String cookieName) {
		return CookieUtils.getCookie(getHttpServletRequest(), cookieName);
	}

	public static String getCookieValue(String cookieName) {
		return CookieUtils.getCookieValue(getHttpServletRequest(), cookieName);
	}

	static final int SECONDS_PER_DAY = 24 * 60 * 60;

	public static Cookie setCookie(String cookieName, String value) {
		return setCookie(cookieName, value, "/", SECONDS_PER_DAY * 30);
	}

	public static Cookie setCookie(String cookieName, String value,
			String path, int maxAge) {
		return CookieUtils.setCookie(WicketUtils.getHttpServletRequest(),
				WicketUtils.getHttpServletResponse(), cookieName, value,
				maxAge, path);

	}

	public static void removeCookie(String cookieName) {
		CookieUtils.invalidateCookie(getHttpServletRequest(),
				getHttpServletResponse(), cookieName);
	}

	public static void removeCookie(String cookieName, String path) {
		CookieUtils.invalidateCookie(getHttpServletRequest(),
				getHttpServletResponse(), cookieName, path);
	}

	public static PageParameters newPageParams(String key, Object value) {
		PageParameters params = new PageParameters();
		params.add(key, value.toString());
		return params;
	}

	public static PageParameters newPageParams(String key1, String value1,
			String key2, String value2) {
		return newPageParams(ImmutableMap.of(key1, value1, key2, value2));
	}

	public static PageParameters newPageParams(String k1, String v1, String k2,
			String v2, String k3, String v3) {
		return newPageParams(ImmutableMap.of(k1, v1, k2, v2, k3, v3));
	}

	public static PageParameters newPageParams(Map<String, String> map) {
		PageParameters params = new PageParameters();
		for (Map.Entry<String, String> each : map.entrySet()) {
			params.add(each.getKey(), each.getValue());
		}

		return params;
	}

	public static String getStringParam(PageParameters params, String key,
			String defaultValue) {
		StringValue v = params.get(key);
		if (v == null)
			return defaultValue;

		return v.toString(defaultValue);
	}

	public static String getStringParam(PageParameters params, String key) {
		return getStringParam(params, key, null);
	}

	public static int getIntParam(PageParameters params, String key,
			int defaultValue) {
		StringValue v = params.get(key);
		if (v == null)
			return defaultValue;

		return v.toInt(defaultValue);
	}
}
