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

import org.apache.wicket.protocol.ws.api.AbstractWebSocketConnection;
import org.apache.wicket.protocol.ws.api.AbstractWebSocketProcessor;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.util.lang.Args;
import org.eclipse.jetty.websocket.api.Session;

/**
 * A wrapper around Jetty9's native WebSocketConnection.
 *
 * @since 6.2
 */
public class WebSocketConnection extends AbstractWebSocketConnection
{
	private final Session session;

	private final PageKey pageKey;
	
	/**
	 * Constructor.
	 *
	 * @param session
	 *            the jetty websocket connection
	 */
	public WebSocketConnection(Session session, AbstractWebSocketProcessor webSocketProcessor, PageKey pageKey)
	{
		super(webSocketProcessor);
		this.session = Args.notNull(session, "connection");
		this.pageKey = pageKey;
	}

	@Override
	public boolean isOpen()
	{
		return session.isOpen();
	}

	@Override
	public void close(int code, String reason)
	{
		if (isOpen())
		{
			session.close(code, reason);
		}
	}

	public PageKey getPageKey() {
		return pageKey;
	}

	@Override
	public IWebSocketConnection sendMessage(String message) throws IOException
	{
		checkClosed();

		session.getRemote().sendStringByFuture(message);
		return this;
	}

	@Override
	public IWebSocketConnection sendMessage(byte[] message, int offset, int length)
		throws IOException
	{
		checkClosed();

		ByteBuffer buf = ByteBuffer.wrap(message, offset, length);
		session.getRemote().sendBytesByFuture(buf);
		return this;
	}

	private void checkClosed()
	{
		if (!isOpen())
		{
			throw new IllegalStateException("The connection is closed.");
		}
	}
}
