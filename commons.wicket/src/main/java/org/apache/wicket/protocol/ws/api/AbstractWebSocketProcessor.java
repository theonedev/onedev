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
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Application;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.page.IPageManager;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.protocol.ws.IWebSocketSettings;
import org.apache.wicket.protocol.ws.WebSocketSettings;
import org.apache.wicket.protocol.ws.api.event.WebSocketBinaryPayload;
import org.apache.wicket.protocol.ws.api.event.WebSocketClosedPayload;
import org.apache.wicket.protocol.ws.api.event.WebSocketConnectedPayload;
import org.apache.wicket.protocol.ws.api.event.WebSocketPayload;
import org.apache.wicket.protocol.ws.api.event.WebSocketPushPayload;
import org.apache.wicket.protocol.ws.api.event.WebSocketTextPayload;
import org.apache.wicket.protocol.ws.api.message.BinaryMessage;
import org.apache.wicket.protocol.ws.api.message.ClosedMessage;
import org.apache.wicket.protocol.ws.api.message.ConnectedMessage;
import org.apache.wicket.protocol.ws.api.message.IWebSocketMessage;
import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;
import org.apache.wicket.protocol.ws.api.message.TextMessage;
import org.apache.wicket.protocol.ws.api.registry.IKey;
import org.apache.wicket.protocol.ws.api.registry.IWebSocketConnectionRegistry;
import org.apache.wicket.protocol.ws.api.registry.PageIdKey;
import org.apache.wicket.protocol.ws.api.registry.ResourceNameKey;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.cycle.RequestCycleContext;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.SharedResourceReference;
import org.apache.wicket.session.ISessionStore;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Checks;
import org.apache.wicket.util.lang.Classes;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.wicket.websocket.WebSocketMessage;

/**
 * The base implementation of IWebSocketProcessor. Provides the common logic
 * for registering a web socket connection and broadcasting its events.
 *
 * @since 6.0
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractWebSocketProcessor implements IWebSocketProcessor
{
	private static final Logger LOG = LoggerFactory.getLogger(AbstractWebSocketProcessor.class);

	/**
	 * A pageId indicating that the endpoint is WebSocketResource
	 */
	private static final int NO_PAGE_ID = -1;

	private static final Method GET_FILTER_PATH_METHOD;
	static
	{
		try
		{
			GET_FILTER_PATH_METHOD = WicketFilter.class.getDeclaredMethod("getFilterPath", new Class[]{});
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		GET_FILTER_PATH_METHOD.setAccessible(true);
	}


	private final WebRequest webRequest;
	private final int pageId;
	private final String resourceName;
	private final Url baseUrl;
	private final WebApplication application;
	private final String sessionId;
	private final IWebSocketConnectionRegistry connectionRegistry;

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
		this.sessionId = request.getSession(true).getId();

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
		this.webRequest = new WebSocketRequest(new ServletRequestCopy(request), getFilterPath(wicketFilter));

		this.application = Args.notNull(application, "application");
		IWebSocketSettings webSocketSettings = IWebSocketSettings.Holder.get(application);
		this.connectionRegistry = webSocketSettings.getConnectionRegistry();
	}

	private String getFilterPath(WicketFilter wicketFilter)
	{
		String filterPath;
		try
		{
			filterPath = (String) GET_FILTER_PATH_METHOD.invoke(wicketFilter);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		return filterPath;
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
	 * A helper that registers the opened connection in the application-level
	 * registry.
	 *
	 * @param connection
	 *      the web socket connection to use to communicate with the client
	 * @see #onOpen(Object)
	 */
	protected final void onConnect(final IWebSocketConnection connection)
	{
		IKey key = getRegistryKey();
		connectionRegistry.setConnection(getApplication(), getSessionId(), key, connection);
		broadcastMessage(new ConnectedMessage(getApplication(), getSessionId(), key));
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

		if (connection != null && connection.isOpen())
		{
			Application oldApplication = ThreadContext.getApplication();
			Session oldSession = ThreadContext.getSession();
			RequestCycle oldRequestCycle = ThreadContext.getRequestCycle();

			WebSocketResponse webResponse = new WebSocketResponse(connection);
			try
			{
				RequestCycle requestCycle;
				if (oldRequestCycle == null || message instanceof IWebSocketPushMessage)
				{
					RequestCycleContext context = new RequestCycleContext(webRequest, webResponse,
							application.getRootRequestMapper(), application.getExceptionMapperProvider().get());

					requestCycle = application.getRequestCycleProvider().get(context);
					requestCycle.getUrlRenderer().setBaseUrl(baseUrl);
					ThreadContext.setRequestCycle(requestCycle);
				}
				else
				{
					requestCycle = oldRequestCycle;
				}

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

				IPageManager pageManager = session.getPageManager();
				try
				{
					Page page = getPage(pageManager);

					WebSocketRequestHandler requestHandler = new WebSocketRequestHandler(page, connection);

					WebSocketPayload payload = createEventPayload(message, requestHandler);

					sendPayload(payload, page);

					if (!(message instanceof ConnectedMessage || message instanceof ClosedMessage))
					{
						requestHandler.respond(requestCycle);
					}
				}
				finally
				{
					pageManager.commitRequest();
				}
			}
			catch (Exception x)
			{
				try {
					WebSocketMessage wsMessage = new WebSocketMessage(WebSocketMessage.ERROR_MESSAGE, x.getMessage());
					String errorMessage = AppLoader.getInstance(ObjectMapper.class).writeValueAsString(wsMessage);
					connection.sendMessage(errorMessage);
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

	/**
	 * Sends the payload either to the page (and its WebSocketBehavior)
	 * or to the WebSocketResource with name {@linkplain #resourceName}
	 *
	 * @param payload
	 *          The payload with the web socket message
	 * @param page
	 *          The page that owns the WebSocketBehavior, in case of behavior usage
	 */
	private void sendPayload(final WebSocketPayload payload, final Page page)
	{
		final Runnable action = new Runnable()
		{
			@Override
			public void run()
			{
				if (pageId != NO_PAGE_ID)
				{
					page.send(application, Broadcast.BREADTH, payload);
				} else
				{
					ResourceReference reference = new SharedResourceReference(resourceName);
					IResource resource = reference.getResource();
					if (resource instanceof WebSocketResource)
					{
						WebSocketResource wsResource = (WebSocketResource) resource;
						wsResource.onPayload(payload);
					} else
					{
						throw new IllegalStateException(
								String.format("Shared resource with name '%s' is not a %s but %s",
										resourceName, WebSocketResource.class.getSimpleName(),
										Classes.name(resource.getClass())));
					}
				}
			}
		};

		IWebSocketSettings webSocketSettings = IWebSocketSettings.Holder.get(application);
		if (webSocketSettings instanceof WebSocketSettings)
		{
			WebSocketSettings wss = (WebSocketSettings) webSocketSettings;
			wss.getSendPayloadExecutor().run(action);
		}
		else
		{
			action.run();
		}
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

	private IKey getRegistryKey()
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
