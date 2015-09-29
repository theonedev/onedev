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
package com.pmease.commons.wicket.websocket;

import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.ws.api.AbstractWebSocketProcessor;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.commons.hibernate.UnitOfWork;
import com.pmease.commons.loader.AppLoader;

/**
 * An {@link org.apache.wicket.protocol.ws.api.IWebSocketProcessor processor} that integrates with
 * Jetty 9.x {@link Session web socket} implementation.
 *
 * @since 6.2
 */
public class WebSocketProcessor extends AbstractWebSocketProcessor implements WebSocketListener {
	
	private static final Logger logger = LoggerFactory.getLogger(WebSocketProcessor.class);

	private final ServletUpgradeRequest request;
	
	/**
	 * Constructor.
	 *
	 * @param upgradeRequest
	 *            the jetty upgrade request
	 * @param upgradeResponse
	 *            the jetty upgrade response
	 * @param application
	 *            the current Wicket Application
	 */
	public WebSocketProcessor(final ServletUpgradeRequest upgradeRequest,
		final ServletUpgradeResponse upgradeResponse, final WebApplication application) {
		super(upgradeRequest.getHttpServletRequest(), application);
		
		this.request = upgradeRequest;
	}

	@Override
	public void onWebSocketConnect(final Session session) {
		run(new Runnable() {

			@Override
			public void run() {
				onConnect(new WebSocketConnection(session, WebSocketProcessor.this));
			}
			
		});
	}
	
	private void run(Runnable runnable) {
		UnitOfWork unitOfWork = AppLoader.getInstance(UnitOfWork.class);
		unitOfWork.begin();
		try {
			PrincipalCollection principals = (PrincipalCollection) request.getSession().getAttribute(
					DefaultSubjectContext.PRINCIPALS_SESSION_KEY);
			if (principals == null)
				principals = new SimplePrincipalCollection(0L, "");
	    	WebSecurityManager securityManager = AppLoader.getInstance(WebSecurityManager.class);
	        ThreadContext.bind(new Subject.Builder(securityManager).principals(principals).buildSubject());

	        runnable.run();
		} finally {
			ThreadContext.unbindSubject();
			unitOfWork.end();
		}
	}
	
	@Override
	public void onWebSocketText(final String message) {
		run(new Runnable() {

			@Override
			public void run() {
				onMessage(message);
			}
			
		});
	}

	@Override
	public void onWebSocketBinary(final byte[] payload, final int offset, final int len) {
		run(new Runnable() {

			@Override
			public void run() {
				onMessage(payload, offset, len);
			}
			
		});
	}

	@Override
	public void onWebSocketClose(final int statusCode, final String reason) {
		run(new Runnable() {

			@Override
			public void run() {
				onClose(statusCode, reason);
			}
			
		});
	}

	@Override
	public void onWebSocketError(Throwable throwable) {
		logger.error("An error occurred when using WebSocket.", throwable);
	}

	@Override
	public void onOpen(Object connection) {
		if (!(connection instanceof Session)) {
			throw new IllegalArgumentException(WebSocketProcessor.class.getName() +
				" can work only with " + Session.class.getName());
		}
		onWebSocketConnect((Session)connection);
	}
}