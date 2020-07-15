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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.wicket.Application;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.page.IPageManager;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.protocol.ws.WebSocketSettings;
import org.apache.wicket.protocol.ws.api.event.WebSocketAbortedPayload;
import org.apache.wicket.protocol.ws.api.event.WebSocketBinaryPayload;
import org.apache.wicket.protocol.ws.api.event.WebSocketClosedPayload;
import org.apache.wicket.protocol.ws.api.event.WebSocketConnectedPayload;
import org.apache.wicket.protocol.ws.api.event.WebSocketPayload;
import org.apache.wicket.protocol.ws.api.event.WebSocketPushPayload;
import org.apache.wicket.protocol.ws.api.event.WebSocketTextPayload;
import org.apache.wicket.protocol.ws.api.message.AbortedMessage;
import org.apache.wicket.protocol.ws.api.message.BinaryMessage;
import org.apache.wicket.protocol.ws.api.message.ClosedMessage;
import org.apache.wicket.protocol.ws.api.message.ConnectedMessage;
import org.apache.wicket.protocol.ws.api.message.ErrorMessage;
import org.apache.wicket.protocol.ws.api.message.IWebSocketMessage;
import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;
import org.apache.wicket.protocol.ws.api.message.TextMessage;
import org.apache.wicket.protocol.ws.api.registry.IKey;
import org.apache.wicket.protocol.ws.api.registry.IWebSocketConnectionRegistry;
import org.apache.wicket.protocol.ws.api.registry.PageIdKey;
import org.apache.wicket.protocol.ws.api.registry.ResourceNameKey;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.cycle.RequestCycleContext;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.session.ISessionStore;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Checks;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.server.OneDev;
import io.onedev.server.web.websocket.WebSocketManager;

/**
 * The base implementation of IWebSocketProcessor. Provides the common logic
 * for registering a web socket connection and broadcasting its events.
 *
 * @since 6.0
 */
public abstract class AbstractWebSocketProcessor implements IWebSocketProcessor
{
	private static final Logger LOG = LoggerFactory.getLogger(AbstractWebSocketProcessor.class);

	/**
	 * A pageId indicating that the endpoint is WebSocketResource
	 */
	static final int NO_PAGE_ID = -1;

	private final WebRequest webRequest;
	private final int pageId;
	private final String resourceName;
	private final Url baseUrl;
	private final WebApplication application;
	private final String sessionId;
	private final WebSocketSettings webSocketSettings;
	private final IWebSocketConnectionRegistry connectionRegistry;
	private final IWebSocketConnectionFilter connectionFilter;
	private final HttpServletRequest servletRequest;

	/**
	 * Constructor.
	 *
	 * @param request
	 *      the http request that was used to create the TomcatWebSocketProcessor
	 * @param application
	 *      the current Wicket Application
	 */
	public AbstractWebSocketProcessor(final HttpServletRequest request, final WebApplication application)
	{
		final HttpSession httpSession = request.getSession(true);
		if (httpSession == null)
		{
			throw new IllegalStateException("There is no HTTP Session bound. Without a session Wicket won't be " +
					"able to find the stored page to update its components");
		}
		this.sessionId = httpSession.getId();

		String pageId = request.getParameter("pageId");
		resourceName = request.getParameter("resourceName");
		if (Strings.isEmpty(pageId) && Strings.isEmpty(resourceName))
		{
			throw new IllegalArgumentException("The request should have either 'pageId' or 'resourceName' parameter!");
		}
		if (Strings.isEmpty(pageId) == false)
		{
			this.pageId = Integer.parseInt(pageId, 10);
		}
		else
		{
			this.pageId = NO_PAGE_ID;
		}

		String baseUrl = request.getParameter(WebRequest.PARAM_AJAX_BASE_URL);
		Checks.notNull(baseUrl, String.format("Request parameter '%s' is required!", WebRequest.PARAM_AJAX_BASE_URL));
		this.baseUrl = Url.parse(baseUrl);

		WicketFilter wicketFilter = application.getWicketFilter();
		this.servletRequest = new ServletRequestCopy(request);

		this.application = Args.notNull(application, "application");

		this.webSocketSettings = WebSocketSettings.Holder.get(application);

		this.webRequest = webSocketSettings.newWebSocketRequest(request, wicketFilter.getFilterPath());

		this.connectionRegistry = webSocketSettings.getConnectionRegistry();

		this.connectionFilter = webSocketSettings.getConnectionFilter();
	}

	@Override
	public void onMessage(final String message)
	{
		broadcastMessage(new TextMessage(message));
	}

	@Override
	public void onMessage(byte[] data, int offset, int length)
	{
		BinaryMessage binaryMessage = new BinaryMessage(data, offset, length);
		broadcastMessage(binaryMessage);
	}

	/**
	 * A helper that registers the opened connection in the application-level registry.
	 *
	 * @param connection
	 *            the web socket connection to use to communicate with the client
	 * @see #onOpen(Object)
	 */
	protected final void onConnect(final IWebSocketConnection connection) {
		IKey key = getRegistryKey();
		connectionRegistry.setConnection(getApplication(), getSessionId(), key, connection);

		if (connectionFilter != null)
		{
			ConnectionRejected connectionRejected = connectionFilter.doFilter(servletRequest);
			if (connectionRejected != null)
			{
				broadcastMessage(new AbortedMessage(getApplication(), getSessionId(), key));
				connectionRegistry.removeConnection(getApplication(), getSessionId(), key);
				connection.close(connectionRejected.getCode(), connectionRejected.getReason());
				return;
			}
		}

		broadcastMessage(new ConnectedMessage(getApplication(), getSessionId(), key));		
		OneDev.getInstance(WebSocketManager.class).onConnect(connection);
	}

	@Override
	public void onClose(int closeCode, String message)
	{
		IKey key = getRegistryKey();
		broadcastMessage(new ClosedMessage(getApplication(), getSessionId(), key));
		connectionRegistry.removeConnection(getApplication(), getSessionId(), key);
	}

	/**
	 * Exports the Wicket thread locals and broadcasts the received message from the client to all
	 * interested components and behaviors in the page with id {@code #pageId}
	 * <p>
	 *     Note: ConnectedMessage and ClosedMessage messages are notification-only. I.e. whatever the
	 *     components/behaviors write in the WebSocketRequestHandler will be ignored because the protocol
	 *     doesn't expect response from the user.
	 * </p>
	 *
	 * @param message
	 *      the message to broadcast
	 */
	public final void broadcastMessage(final IWebSocketMessage message)
	{
		IKey key = getRegistryKey();
		IWebSocketConnection connection = connectionRegistry.getConnection(application, sessionId, key);

		if (connection != null && (connection.isOpen() || isSpecialMessage(message))) 
		{
			Application oldApplication = ThreadContext.getApplication();
			Session oldSession = ThreadContext.getSession();
			RequestCycle oldRequestCycle = ThreadContext.getRequestCycle();

			WebResponse webResponse = webSocketSettings.newWebSocketResponse(connection);
			try
			{
				WebSocketRequestMapper requestMapper = new WebSocketRequestMapper(application.getRootRequestMapper());
				RequestCycle requestCycle = createRequestCycle(requestMapper, webResponse);
				ThreadContext.setRequestCycle(requestCycle);

				ThreadContext.setApplication(application);

				Session session;
				if (oldSession == null || message instanceof IWebSocketPushMessage)
				{
					ISessionStore sessionStore = application.getSessionStore();
					session = sessionStore.lookup(webRequest);
					ThreadContext.setSession(session);
				}
				else
				{
					session = oldSession;
				}

				if (session == null)
				{
					connectionRegistry.removeConnection(application, sessionId, key);
					LOG.debug("No Session could be found for session id '{}' and key '{}'!", sessionId, key);
					return;
				}
				
				IPageManager pageManager = session.getPageManager();
				Page page = getPage(pageManager);

				if (page != null) 
				{
					WebSocketRequestHandler requestHandler = webSocketSettings.newWebSocketRequestHandler(page, connection);

					@SuppressWarnings("rawtypes")
					WebSocketPayload payload = createEventPayload(message, requestHandler);

					if (!(message instanceof ConnectedMessage || isSpecialMessage(message))) 
					{
						requestCycle.scheduleRequestHandlerAfterCurrent(requestHandler);
					}

					IRequestHandler broadcastingHandler = new WebSocketMessageBroadcastHandler(pageId, resourceName, payload);
					requestMapper.setHandler(broadcastingHandler);
					requestCycle.processRequestAndDetach();
				} else {
					LOG.debug("Page with id '{}' has been expired. No message will be broadcast!", pageId);
				}
			}
			catch (Exception x)
			{
				try {
					connection.sendMessage(WebSocketManager.ERROR_MESSAGE);
				} catch (IOException e1) {
				}
				LOG.error("An error occurred during processing of a WebSocket message", x);
			}
			finally
			{
				try
				{
					webResponse.close();
				}
				finally
				{
					ThreadContext.setApplication(oldApplication);
					ThreadContext.setRequestCycle(oldRequestCycle);
					ThreadContext.setSession(oldSession);
				}
			}
		}
		else
		{
			LOG.debug("Either there is no connection({}) or it is closed.", connection);
		}
	}

	private static boolean isSpecialMessage(IWebSocketMessage message)
	{
		return message instanceof ClosedMessage || message instanceof ErrorMessage || message instanceof AbortedMessage;
	}

	private RequestCycle createRequestCycle(WebSocketRequestMapper requestMapper, WebResponse webResponse)
	{
		RequestCycleContext context = new RequestCycleContext(webRequest, webResponse,
				requestMapper, application.getExceptionMapperProvider().get());

		RequestCycle requestCycle = application.getRequestCycleProvider().get(context);
		requestCycle.getListeners().add(application.getRequestCycleListeners());
		requestCycle.getListeners().add(new AbstractRequestCycleListener()
		{
			@Override
			public void onDetach(final RequestCycle requestCycle)
			{
				if (Session.exists())
				{
					Session.get().getPageManager().commitRequest();
				}
			}
		});
		requestCycle.getUrlRenderer().setBaseUrl(baseUrl);
		return requestCycle;
	}

	/**
	 * @param pageManager
	 *      the page manager to use when finding a page by id
	 * @return the page to use when creating WebSocketRequestHandler
	 */
	private Page getPage(IPageManager pageManager)
	{
		Page page;
		if (pageId != -1)
		{
			page = (Page) pageManager.getPage(pageId);
		}
		else
		{
			page = new WebSocketResourcePage();
		}
		return page;
	}

	protected final WebApplication getApplication()
	{
		return application;
	}

	protected final String getSessionId()
	{
		return sessionId;
	}

	@SuppressWarnings("rawtypes")
	private WebSocketPayload createEventPayload(IWebSocketMessage message, WebSocketRequestHandler handler)
	{
		final WebSocketPayload payload;
		if (message instanceof TextMessage)
		{
			payload = new WebSocketTextPayload((TextMessage) message, handler);
		}
		else if (message instanceof BinaryMessage)
		{
			payload = new WebSocketBinaryPayload((BinaryMessage) message, handler);
		}
		else if (message instanceof ConnectedMessage)
		{
			payload = new WebSocketConnectedPayload((ConnectedMessage) message, handler);
		}
		else if (message instanceof ClosedMessage)
		{
			payload = new WebSocketClosedPayload((ClosedMessage) message, handler);
		}
		else if (message instanceof AbortedMessage)
		{
			payload = new WebSocketAbortedPayload((AbortedMessage) message, handler);
		}
		else if (message instanceof IWebSocketPushMessage)
		{
			payload = new WebSocketPushPayload((IWebSocketPushMessage) message, handler);
		}
		else
		{
			throw new IllegalArgumentException("Unsupported message type: " + message.getClass().getName());
		}
		return payload;
	}

	protected IKey getRegistryKey()
	{
		IKey key;
		if (Strings.isEmpty(resourceName))
		{
			key = new PageIdKey(pageId);
		}
		else
		{
			key = new ResourceNameKey(resourceName);
		}
		return key;
	}

	/**
	 * A dummy page that is used to create a new WebSocketRequestHandler for
	 * web socket connections to WebSocketResource
	 */
	@SuppressWarnings("serial")
	private static class WebSocketResourcePage extends WebPage implements IMarkupResourceStreamProvider
	{
		private WebSocketResourcePage()
		{
			setStatelessHint(true);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass)
		{
			return new StringResourceStream("");
		}
	}
}
