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
import java.nio.ByteBuffer;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.apache.wicket.protocol.ws.api.AbstractWebSocketProcessor;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.Callback;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.server.OneDev;
import io.onedev.server.persistence.SessionService;

/**
 * A wrapper around Jetty9's native WebSocketConnection.
 *
 * @since 6.2
 */
public class WebSocketConnection extends org.apache.wicket.protocol.ws.api.AbstractWebSocketConnection {

	private final Logger logger = LoggerFactory.getLogger(WebSocketConnection.class);
	
	private final AbstractWebSocketProcessor webSocketProcessor;

	private final Session session;

	private final PageKey pageKey;

	private final Subject subject;

	private final Set<IWebSocketPushMessage> messageQueue = new LinkedHashSet<>();

	private boolean working;
	
	public WebSocketConnection(Session session, AbstractWebSocketProcessor webSocketProcessor, PageKey pageKey, Subject subject)
	{
		super(webSocketProcessor);
		this.webSocketProcessor = webSocketProcessor;
		this.session = session;
		this.pageKey = pageKey;
		this.subject = subject;
	}

	@Override
	public boolean isOpen() {
		return session.isOpen();
	}

	public Session getSession() {
		return session;
	}

	@Override
	public void close(int code, String reason) {
		if (isOpen()) {
			session.close(code, reason, Callback.NOOP);
		}
	}

	public PageKey getPageKey() {
		return pageKey;
	}

	@Override
	public IWebSocketConnection sendMessage(String message) throws IOException {
		checkClosed();

		session.sendText(message, Callback.NOOP);
		return this;
	}

	@Override
	public IWebSocketConnection sendMessage(byte[] message, int offset, int length) throws IOException {
		checkClosed();

		ByteBuffer buf = ByteBuffer.wrap(message, offset, length);
		session.sendBinary(buf, Callback.NOOP);
		return this;
	}

    @Override
    public java.util.concurrent.Future<Void> sendMessageAsync(String message) {
        return sendMessageAsync(message, -1);
    }

    @Override
    public java.util.concurrent.Future<Void> sendMessageAsync(String message, long timeout) {
        var callback = new Callback.Completable();
        session.sendText(message, callback);
        if (timeout >= 0)
            callback.orTimeout(timeout, java.util.concurrent.TimeUnit.MILLISECONDS);
        return callback;
    }

    @Override
    public java.util.concurrent.Future<Void> sendMessageAsync(byte[] message, int offset, int length) {
        return sendMessageAsync(message, offset, length, -1);
    }

    @Override
    public java.util.concurrent.Future<Void> sendMessageAsync(byte[] message, int offset, int length, long timeout) {
        var callback = new Callback.Completable();
        session.sendBinary(ByteBuffer.wrap(message, offset, length), callback);
        if (timeout >= 0)
            callback.orTimeout(timeout, java.util.concurrent.TimeUnit.MILLISECONDS);
        return callback;
    }

	private void checkClosed() {
		if (!isOpen()) {
			throw new IllegalStateException("The connection is closed.");
		}
	}

	@Override
	public void sendMessage(IWebSocketPushMessage message) {
		webSocketProcessor.broadcastMessage(message);
	}

	public synchronized boolean queueMessage(IWebSocketPushMessage message) {
		return messageQueue.add(message);
	}

	public synchronized void checkMessageQueue() {
		if (!messageQueue.isEmpty() && !working) {
			ThreadContext.bind(subject);
			working = true;

			OneDev.getInstance(ExecutorService.class).execute(new Runnable() {

				@Nullable
				private IWebSocketPushMessage getNextMessage() {
					synchronized (WebSocketConnection.this) {
						var it = messageQueue.iterator();
						if (it.hasNext()) {
							var message = it.next();
							it.remove();
							return message;
						} else {
							return null;
						}
					}	
				}

				@Override
				public void run() {
					try {
						while (true) {
							IWebSocketPushMessage message = getNextMessage();
							if (message != null) {
								/*
								 * Process each message in a separate session to make sure database entities 
								 * are refreshed 
								 */
								OneDev.getInstance(SessionService.class).run(new Runnable() {
	
									@Override
									public void run() {
										webSocketProcessor.broadcastMessage(message);
									}
								});
							} else {
								break;
							}
						}
					} catch (Throwable e) {
						logger.error("Error processing websocket message", e);
						try {
							sendMessage(WebSocketMessages.ERROR_MESSAGE);
						} catch (Throwable e2) {
						}
					} finally {
						synchronized (WebSocketConnection.this) {
							working = false;
						}
					}
				}
			});
		}
	}
	
}
