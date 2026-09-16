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

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.apache.wicket.Component;
import org.apache.wicket.core.request.handler.AbstractPartialPageRequestHandler;
import org.apache.wicket.core.request.handler.logger.PageLogData;
import org.apache.wicket.page.PartialPageUpdate;
import org.apache.wicket.page.XmlPartialPageUpdate;
import org.apache.wicket.request.ILogData;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.Response;
import io.onedev.server.OneDev;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.websocket.WebSocketService;
import org.apache.wicket.response.StringResponse;
import org.apache.wicket.util.lang.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A handler of WebSocket requests.
 *
 * @since 6.0
 */
public class WebSocketRequestHandler extends AbstractPartialPageRequestHandler implements IWebSocketRequestHandler
{
	private static final Logger LOG = LoggerFactory.getLogger(WebSocketRequestHandler.class);


	private final IWebSocketConnection connection;

	private PartialPageUpdate update;

	private PageLogData logData;

	public WebSocketRequestHandler(final Component component, final IWebSocketConnection connection)
	{
		super(Args.notNull(component, "component").getPage());
		this.connection = Args.notNull(connection, "connection");
	}

	@Override
	public void push(CharSequence message)
	{
		if (connection.isOpen())
		{
			Args.notNull(message, "message");
			try
			{
				connection.sendMessage(message.toString());
			} catch (IOException iox)
			{
				LOG.error("An error occurred while pushing text message.", iox);
			}
		}
		else
		{
			LOG.warn("The websocket connection is already closed. Cannot push the text message '{}'", message);
		}
	}

	@Override
	public Future<Void> pushAsync(CharSequence message, long timeout)
	{
		if (connection.isOpen())
		{
			Args.notNull(message, "message");
			return connection.sendMessageAsync(message.toString(), timeout);
		}
		else
		{
			LOG.warn("The websocket connection is already closed. Cannot push the text message '{}'", message);
		}
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public Future<Void> pushAsync(CharSequence message)
	{
		return pushAsync(message, -1);
	}

	@Override
	public void push(byte[] message, int offset, int length)
	{
		if (connection.isOpen())
		{
			Args.notNull(message, "message");
			try
			{
				connection.sendMessage(message, offset, length);
			} catch (IOException iox)
			{
				LOG.error("An error occurred while pushing binary message.", iox);
			}
		}
		else
		{
			LOG.warn("The websocket connection is already closed. Cannot push the binary message '{}'", message);
		}
	}

	@Override
	public Future<Void> pushAsync(byte[] message, int offset, int length)
	{
		return pushAsync(message, offset, length, -1);
	}

	@Override
	public Future<Void> pushAsync(byte[] message, int offset, int length, long timeout)
	{
		if (connection.isOpen())
		{
			Args.notNull(message, "message");
			return connection.sendMessageAsync(message, offset, length, timeout);
		}
		else
		{
			LOG.warn("The websocket connection is already closed. Cannot push the binary message '{}'", message);
		}
		return CompletableFuture.completedFuture(null);
	}

	/**
	 * @return if <code>true</code> then EMPTY partial updates will se send. If <code>false</code> then EMPTY
	 *    partial updates will be skipped. A possible use case is: a page receives and a push event but no one is
	 *    listening to it, and nothing is added to {@link org.apache.wicket.protocol.ws.api.WebSocketRequestHandler}
	 *    thus no real push to client is needed. For compatibilities this is set to true. Thus EMPTY updates are sent
	 *    by default.
	 */
	protected boolean shouldPushWhenEmpty()
	{
		return true;
	}

	protected PartialPageUpdate getUpdate() {
		if (update == null) {
			update = new XmlPartialPageUpdate(getPage()) {

				@Override
				protected void onBeforeRespond(Response response) {
					super.onBeforeRespond(response);
					BasePage page = (BasePage) getPage();
					if (page.getSessionFeedback().anyMessage())
						WebSocketRequestHandler.this.add(page.getSessionFeedback());
					
					for (Component component: markupIdToComponent.values()) {
						prependJavaScript((String.format("$(document).trigger('beforeElementReplace', '%s');", component.getMarkupId())));
						appendJavaScript((String.format("$(document).trigger('afterElementReplace', '%s');", component.getMarkupId())));
					}
				}

				@Override
				protected void onAfterRespond(Response response) {
					if (!markupIdToComponent.isEmpty())
						OneDev.getInstance(WebSocketService.class).observe((BasePage) getPage());
					super.onAfterRespond(response);
				}
				
			};
		}
		return update;
	}


	@Override
	public Collection<? extends Component> getComponents()
	{
		if (update == null) {
			return Collections.emptyList();
		} else {
			return update.getComponents();
		}
	}

	@Override
	public ILogData getLogData()
	{
		return logData;
	}


	@Override
	public void respond(IRequestCycle requestCycle)
	{
        if (update != null && (shouldPushWhenEmpty() || !update.isEmpty())) {
            // see WICKET-7098
            // A malformed XML is generated if a runtime exception happen during rendering phase of a web
            // socket push request. Writing to a buffer allows to generate a proper XML
            // as request's buffer will not be polluted by partial write operations
            StringResponse bodyResponse = new StringResponse();
            // additionally, we use the charset for the request instead of a hardcoded UTF-8
            update.writeTo(bodyResponse, requestCycle.getRequest().getCharset().name());
            requestCycle.getResponse().write(bodyResponse.getBuffer());
        }
	}

	@Override
	public void detach(IRequestCycle requestCycle)
	{
		if (logData == null)
		{
			logData = new PageLogData(getPage());
		}

		if (update != null) {
			update.detach(requestCycle);
			update = null;
		}
	}
}
