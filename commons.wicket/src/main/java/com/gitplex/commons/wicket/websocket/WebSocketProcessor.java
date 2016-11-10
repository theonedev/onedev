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
package com.gitplex.commons.wicket.websocket;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.ws.api.AbstractWebSocketProcessor;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitplex.commons.hibernate.UnitOfWork;
import com.gitplex.commons.loader.AppLoader;

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
				PageKey pageKey = new PageKey(getSessionId(), getRegistryKey());
				WebSocketConnection connection = new WebSocketConnection(session, WebSocketProcessor.this, pageKey);
				onConnect(connection);
			}
			
		});
	}
	
	private void run(Runnable runnable) {
		UnitOfWork unitOfWork = AppLoader.getInstance(UnitOfWork.class);
		unitOfWork.begin();
		try {
			Subject subject = (Subject) request.getHttpServletRequest().getAttribute(WebSocketFilter.SHIRO_SUBJECT);
	        ThreadContext.bind(subject);

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
		onWebSocketConnect((Session)connection);
	}
}