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

import static jakarta.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

import org.apache.shiro.subject.Subject;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.ws.api.AbstractWebSocketProcessor;
import org.eclipse.jetty.websocket.api.Session;
import java.nio.ByteBuffer;
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.ee11.websocket.server.JettyServerUpgradeRequest;
import org.eclipse.jetty.ee11.websocket.server.JettyServerUpgradeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.server.OneDev;
import io.onedev.server.exception.ExceptionUtils;
import io.onedev.server.persistence.SessionService;

/**
 * An {@link org.apache.wicket.protocol.ws.api.IWebSocketProcessor processor} that integrates with
 * Jetty 12 {@link Session web socket} implementation.
 *
 * @since 6.2
 */
public class WebSocketProcessor extends AbstractWebSocketProcessor implements Session.Listener.AutoDemanding {
	
	private static final Logger logger = LoggerFactory.getLogger(WebSocketProcessor.class);

	private final Subject subject;
	
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
	public WebSocketProcessor(final JettyServerUpgradeRequest upgradeRequest,
		final JettyServerUpgradeResponse upgradeResponse, final WebApplication application) {
		super(upgradeRequest.getHttpServletRequest(), application);
		
		this.subject = (Subject) upgradeRequest.getHttpServletRequest().getAttribute(WebSocketFilter.SHIRO_SUBJECT);
	}

	@Override
	public void onWebSocketOpen(final Session session) {
        // Jetty 12 defaults to 30 seconds, which races our 30-second keep-alive schedule.
        session.setIdleTimeout(java.time.Duration.ofSeconds(WebSocketService.KEEP_ALIVE_INTERVAL * 3L));
		run(() -> {
			PageKey pageKey = new PageKey(getSessionId(), getRegistryKey());
			WebSocketConnection connection = new WebSocketConnection(session, WebSocketProcessor.this, pageKey, subject);
			onConnect(connection);
		});
	}
	
	private void run(Runnable runnable) {
		SessionService sessionService = OneDev.getInstance(SessionService.class);
        var threadState = new org.apache.shiro.subject.support.SubjectThreadState(subject);
        threadState.bind();
        try {
            sessionService.run(runnable);
        } finally {
            threadState.restore();
        }
	}
	
	@Override
	public void onWebSocketText(final String message) {
		if (!message.equals(WebSocketMessages.KEEP_ALIVE)) 
			run(() -> onMessage(message));
	}

	@Override
	public void onWebSocketBinary(ByteBuffer payload, Callback callback) {
        byte[] bytes = new byte[payload.remaining()];
        payload.get(bytes);
        try {
            run(() -> onMessage(bytes, 0, bytes.length));
            callback.succeed();
        } catch (Throwable t) {
            callback.fail(t);
        }
    }

	@Override
	public void onWebSocketClose(final int statusCode, final String reason) {
		run(() -> onClose(statusCode, reason));
	}

	@Override
	public void onWebSocketError(Throwable throwable) {
		var response = ExceptionUtils.buildResponse(throwable);
		if (response == null || response.getStatus() >= SC_INTERNAL_SERVER_ERROR)
			logger.error("An error occurred when using WebSocket.", throwable);	
	}

	@Override
	public void onOpen(Object connection) {
		onWebSocketOpen((Session)connection);
	}

}
