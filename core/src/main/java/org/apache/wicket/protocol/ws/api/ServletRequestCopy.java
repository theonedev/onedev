/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.protocol.ws.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

/**
 * A copy of the http servlet request used to create the WebSocket.
 * Copy its details because they are discarded at the end of the request (even if we
 * use AsyncContext from Servlet 3 spec). This copy is used later for following requests
 * through the WebSocket connection to construct WebSocketRequest.
 *
 * @since 6.0
 */
public class ServletRequestCopy implements HttpServletRequest
{
	private final String contextPath;
	private final String servletPath;
	private final String pathInfo;
	private final String requestUri;
	private final HttpSessionCopy httpSession;
	private final StringBuffer requestURL;
	private final Map<String, Object> attributes = new HashMap<>();
	private final Map<String, Enumeration<String>> headers = new HashMap<>();
	private final Map<String, String[]> parameters = new HashMap<>();
	private final String method;
	private final String serverName;
	private final int serverPort;
	private final String protocol;
	private final String scheme;
	private final String contentType;
	private final Locale locale;
	private final Enumeration<Locale> locales;
	private final boolean isSecure;
	private final String remoteUser;
	private final String remoteAddr;
	private final String remoteHost;
	private final int remotePort;
	private final String localAddr;
	private final String localName;
	private final int localPort;
	private final String pathTranslated;
	private final String requestedSessionId;
	private final Principal principal;

	private String characterEncoding;

	public ServletRequestCopy(HttpServletRequest request) {
		this.servletPath = request.getServletPath();
		this.contextPath = request.getContextPath();
		this.pathInfo = request.getPathInfo();
		this.requestUri = request.getRequestURI();
		this.requestURL = request.getRequestURL();
		this.method = request.getMethod();
		this.serverName = request.getServerName();
		this.serverPort = request.getServerPort();
		this.protocol = request.getProtocol();
		this.scheme = request.getScheme();
		
		
		/*
		 * have to comment out below two lines as otherwise web socket will
		 * report UnSupportedOperationException upon connection
		 */
		//this.characterEncoding = request.getCharacterEncoding();
		//this.contentType = request.getContentType();
		//this.requestedSessionId = request.getRequestedSessionId();
		this.characterEncoding = null;
		this.contentType = null;
		this.requestedSessionId = null;
		
		this.locale = request.getLocale();
		this.locales = request.getLocales();
		this.isSecure = request.isSecure();
		this.remoteUser = request.getRemoteUser();
		this.remoteAddr = request.getRemoteAddr();
		this.remoteHost = request.getRemoteHost();
		this.remotePort = request.getRemotePort();
		this.localAddr = request.getLocalAddr();
		this.localName = request.getLocalName();
		this.localPort = request.getLocalPort();
		this.pathTranslated = request.getPathTranslated();
		this.principal = request.getUserPrincipal();

		HttpSession session = request.getSession(true);
		httpSession = new HttpSessionCopy(session);

		String s;
		Enumeration<String> e = request.getHeaderNames();
		while (e != null && e.hasMoreElements()) {
			s = e.nextElement();
			Enumeration<String> headerValues = request.getHeaders(s);
			this.headers.put(s, headerValues);
		}

		e = request.getAttributeNames();
		while (e != null && e.hasMoreElements()) {
			s = e.nextElement();
			attributes.put(s, request.getAttribute(s));
		}

		e = request.getParameterNames();
		while (e != null && e.hasMoreElements()) {
			s = e.nextElement();
			parameters.put(s, request.getParameterValues(s));
		}
	}

	@Override
	public String getServerName() {
		return serverName;
	}

	@Override
	public int getServerPort() {
		return serverPort;
	}

	@Override
	public BufferedReader getReader() throws IOException
	{
		return null;
	}

	@Override
	public String getRemoteAddr()
	{
		return remoteAddr;
	}

	@Override
	public String getRemoteHost()
	{
		return remoteHost;
	}

	@Override
	public HttpSession getSession(boolean create) {
		return httpSession;
	}

	@Override
	public String getMethod() {
		return method;
	}

	@Override
	public String getAuthType()
	{
		return null;
	}

	@Override
	public Cookie[] getCookies()
	{
		return new Cookie[0];
	}

	@Override
	public long getDateHeader(String name)
	{
		return 0;
	}

	@Override
	public String getHeader(String name)
	{
		Enumeration<String> values = headers.get(name);
		if (values != null && values.hasMoreElements())
		{
			return values.nextElement();
		}
		return null;
	}

	@Override
	public Enumeration<String> getHeaders(final String name)
	{
		return headers.get(name);
	}

	@Override
	public Enumeration<String> getParameterNames() {
		return Collections.enumeration(parameters.keySet());
	}

	@Override
	public String getParameter(String name) {
		return parameters.get(name) != null ? parameters.get(name)[0] : null;
	}

	@Override
	public String[] getParameterValues(final String name) {
		return parameters.get(name);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map getParameterMap()
	{
		return parameters;
	}

	@Override
	public String getProtocol()
	{
		String _protocol = "ws";
		if ("https".equalsIgnoreCase(protocol))
		{
			_protocol = "wss";
		}
		return _protocol;
	}

	@Override
	public String getScheme()
	{
		String _scheme = "ws";
		if ("https".equalsIgnoreCase(scheme))
		{
			_scheme = "wss";
		}
		return _scheme;
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		return Collections.enumeration(headers.keySet());
	}

	@Override
	public int getIntHeader(String name)
	{
		Enumeration<String> values = headers.get(name);
		int result = -1;
		if (values.hasMoreElements())
		{
			String value = values.nextElement();
			result = Integer.parseInt(value, 10);
		}
		return result;
	}

	@Override
	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return Collections.enumeration(attributes.keySet());
	}

	@Override
	public String getCharacterEncoding()
	{
		return characterEncoding;
	}

	@Override
	public void setCharacterEncoding(String characterEncoding) throws UnsupportedEncodingException
	{
		this.characterEncoding = characterEncoding;
	}

	@Override
	public int getContentLength()
	{
		return 0;
	}

	@Override
	public String getContentType()
	{
		return contentType;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException
	{
		return null;
	}

	@Override
	public void setAttribute(String name, Object o) {
		attributes.put(name, o);
	}

	@Override
	public void removeAttribute(String name) {
		attributes.remove(name);
	}

	@Override
	public Locale getLocale()
	{
		return locale;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Enumeration getLocales()
	{
		return locales;
	}

	@Override
	public boolean isSecure()
	{
		return isSecure;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path)
	{
		return null;
	}

	@Override
	public String getRealPath(String path)
	{
		return null;
	}

	@Override
	public int getRemotePort()
	{
		return remotePort;
	}

	@Override
	public String getLocalName()
	{
		return localName;
	}

	@Override
	public String getLocalAddr()
	{
		return localAddr;
	}

	@Override
	public int getLocalPort()
	{
		return localPort;
	}

	@Override
	public ServletContext getServletContext()
	{
		return null;
	}

	@Override
	public AsyncContext startAsync() throws IllegalStateException
	{
		return null;
	}

	@Override
	public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException
	{
		return null;
	}

	@Override
	public boolean isAsyncStarted()
	{
		return false;
	}

	@Override
	public boolean isAsyncSupported()
	{
		return false;
	}

	@Override
	public AsyncContext getAsyncContext()
	{
		return null;
	}

	@Override
	public DispatcherType getDispatcherType()
	{
		return null;
	}

	@Override
	public String getContextPath() {
		return contextPath;
	}

	@Override
	public String getQueryString()
	{
		return null;
	}

	@Override
	public String getRemoteUser()
	{
		return remoteUser;
	}

	@Override
	public boolean isUserInRole(String role)
	{
		return false;
	}

	@Override
	public Principal getUserPrincipal()
	{
		return principal;
	}

	@Override
	public String getRequestedSessionId()
	{
		return requestedSessionId;
	}

	@Override
	public String getServletPath() {
		return servletPath;
	}

	@Override
	public String getPathInfo() {
		return pathInfo;
	}

	@Override
	public String getPathTranslated()
	{
		return pathTranslated;
	}

	@Override
	public String getRequestURI() {
		return requestUri;
	}

	@Override
	public HttpSession getSession() {
		return httpSession;
	}

	@Override
	public boolean isRequestedSessionIdValid()
	{
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie()
	{
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromURL()
	{
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromUrl()
	{
		return false;
	}

	@Override
	public boolean authenticate(HttpServletResponse response) throws IOException, ServletException
	{
		return false;
	}

	@Override
	public void login(String username, String password) throws ServletException
	{
	}

	@Override
	public void logout() throws ServletException
	{
	}

	@Override
	public Collection<Part> getParts() throws IOException, ServletException
	{
		return Collections.emptyList();
	}

	@Override
	public Part getPart(String name) throws IOException, ServletException
	{
		return null;
	}

	@Override
	public StringBuffer getRequestURL() {
		return requestURL;
	}

	@Override
	public long getContentLengthLong() {
		return 0;
	}

	@Override
	public String changeSessionId() {
		return null;
	}

	@Override
	public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
		return null;
	}
}
