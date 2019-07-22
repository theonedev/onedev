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
package io.onedev.server.web.websocket;

import java.io.IOException;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.protocol.ws.AbstractUpgradeFilter;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.server.WebSocketServerFactory;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An upgrade filter that uses Jetty9's WebSocketServerFactory to decide whether
 * to upgrade or not.
 */
public class WebSocketFilter extends AbstractUpgradeFilter {

	public static final String SHIRO_SUBJECT = "shiro_subject"; 
	
	private static final Logger logger = LoggerFactory.getLogger(WebSocketFilter.class);

	private WebSocketServerFactory webSocketFactory;

	private final WebSocketPolicy webSocketPolicy;

	public WebSocketFilter(WebSocketPolicy webSocketPolicy) {
		this.webSocketPolicy = webSocketPolicy;
	}

	@Override
	public void init(final boolean isServlet, final FilterConfig filterConfig) throws ServletException {
		super.init(isServlet, filterConfig);
 
		try {
			webSocketFactory = new WebSocketServerFactory(getApplication().getServletContext(), webSocketPolicy);

			webSocketFactory.setCreator(new WebSocketCreator() {
				@Override
				public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
					return new WebSocketProcessor(req, resp, getApplication());
				}

			});

			webSocketFactory.start();
		} catch (ServletException x) {
			throw x;
		} catch (Exception x) {
			throw new ServletException(x);
		}
	}

	@Override
	protected boolean acceptWebSocket(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		req.setAttribute(SHIRO_SUBJECT, SecurityUtils.getSubject());
		return super.acceptWebSocket(req, resp) && webSocketFactory.acceptWebSocket(req, resp);
	}

	@Override
	public void destroy() {
		try {
			if (webSocketFactory != null) {
				webSocketFactory.stop();
			}
		} catch (Exception x) {
			logger.warn("A problem occurred while stopping the web socket factory", x);
		}

		super.destroy();
	}
}
