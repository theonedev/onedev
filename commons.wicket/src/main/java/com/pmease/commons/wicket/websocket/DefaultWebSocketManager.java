package com.pmease.commons.wicket.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.wicket.Application;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.protocol.ws.api.registry.IWebSocketConnectionRegistry;
import org.apache.wicket.protocol.ws.api.registry.SimpleWebSocketConnectionRegistry;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.commons.wicket.CommonPage;

@Singleton
public class DefaultWebSocketManager implements WebSocketManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultWebSocketManager.class);
	
	private final Application application;
	
	private final ExecutorService executorService;

	private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
	
	private final WebSocketPolicy webSocketPolicy;
	
	private final int WEBSOCKET_TOLERENCE = 5;
	
	private final Map<PageKey, Collection<WebSocketRegion>> regions = new ConcurrentHashMap<>();
	
	private final IWebSocketConnectionRegistry connectionRegistry = new SimpleWebSocketConnectionRegistry();

	private final Map<PageKey, Date> recentRenderedPages = new ConcurrentHashMap<>();
	
	private final Queue<RecentRenderRequest> recentRenderRequests = new ConcurrentLinkedQueue<>();
	
	@Inject
	public DefaultWebSocketManager(Application application, 
			ExecutorService executorService, WebSocketPolicy webSocketPolicy) {
		this.application = application;
		this.executorService = executorService;
		this.webSocketPolicy = webSocketPolicy;
	}
	
	@Override
	public void onRegionChange(CommonPage page) {
		regions.put(new PageKey(page), page.getWebSocketRegions());
	}
	
	@Override
	public void onDestroySession(String sessionId) {
		for (Iterator<Map.Entry<PageKey, Collection<WebSocketRegion>>> it = regions.entrySet().iterator(); it.hasNext();) {
			if (it.next().getKey().getSessionId().equals(sessionId))
				it.remove();
		}
	}

	@Override
	public void requestToRender(WebSocketRegion region, @Nullable PageKey sourcePageKey, 
			@Nullable PageKey targetPageKey) {
		try {
			if (targetPageKey != null) {
				WebSocketConnection connection = (WebSocketConnection) connectionRegistry.getConnection(
						application, targetPageKey.getSessionId(), targetPageKey.getPageId());
				if (connection != null && connection.isOpen() && containsRegion(connection, region)) {
					connection.sendMessage(RENDER_CALLBACK);
				}
			} else {
				for (IWebSocketConnection connection: connectionRegistry.getConnections(application)) {
					PageKey pageKey = ((WebSocketConnection) connection).getPageKey();
					if (connection.isOpen() 
							&& (sourcePageKey == null || !sourcePageKey.equals(pageKey)) 
							&& containsRegion(connection, region)) {
						connection.sendMessage(RENDER_CALLBACK);
					}
				}
			}
			
			recentRenderRequests.add(new RecentRenderRequest(region, sourcePageKey, targetPageKey, new Date()));
		} catch (Exception e) {
			logger.error("Error sending websocket message", e);
		}
	}
	
	private boolean containsRegion(IWebSocketConnection connection, WebSocketRegion region) {
		PageKey pageKey = ((WebSocketConnection)connection).getPageKey();
		Collection<WebSocketRegion> connectionRegions = regions.get(pageKey);
		if (connectionRegions != null) {
			for (WebSocketRegion connectionRegion: connectionRegions) {
				if (connectionRegion.contains(region)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void onConnect(WebSocketConnection connection) {
		/*
		 * When a new websocket connection arrives, we re-send all sent websocket messages since 
		 * rendering of its hosting page as otherwise the host page might be missing some 
		 * important websocket messages. For instance, when a pull request is opened, a pull 
		 * request integration preview is calculating in the background, and the request page 
		 * will be rendered, and then a websocket connection is established after browser renders
		 * the page. If integration preview calculation finishes after rendering of the request 
		 * detail page and before websocket establishing of request detail page, the integration 
		 * preview area of the page will not be refreshed without below code as it will miss the 
		 * websocket message sent upon completion of integration preview.   
		 * 
		 */
		PageKey connectionPageKey = connection.getPageKey();
		Date renderDate = recentRenderedPages.get(connectionPageKey);
		if (renderDate != null) {
			Collection<String> messagesToSend = new ArrayList<>();
			for (RecentRenderRequest request: recentRenderRequests) {
				WebSocketRegion region = request.getRegion();
				if (request.getDate().getTime() > renderDate.getTime() 
						&& containsRegion(connection, region)) {
					PageKey sourcePageKey = request.getSourcePageKey();
					PageKey targetPageKey = request.getTargetPageKey();
					if (targetPageKey != null) {
						if (connectionPageKey.equals(targetPageKey)) {
							messagesToSend.add(RENDER_CALLBACK);
						}
					} else if (sourcePageKey == null || !sourcePageKey.equals(connectionPageKey)) {
						messagesToSend.add(RENDER_CALLBACK);
					}
				}
			}
			
			if (!messagesToSend.isEmpty()) {
				executorService.execute(new Runnable() {

					@Override
					public void run() {
						for (String message: messagesToSend) {
							try {
								connection.sendMessage(message);
							} catch (IOException e) {
								logger.error("Error sending websocket message");
							}
						}
					}
					
				});
			}
		}
	}
	
	@Override
	public void onRenderPage(CommonPage page) {
		recentRenderedPages.put(new PageKey(page), new Date());
	}

	@Override
	public void start() {
		scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {

			@Override
			public void run() {
				for (IWebSocketConnection connection: new SimpleWebSocketConnectionRegistry().getConnections(application)) {
					if (connection.isOpen()) {
						try {
							connection.sendMessage(WebSocketManager.KEEP_ALIVE);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				}
			}
			
		}, 0, webSocketPolicy.getIdleTimeout()/2, TimeUnit.MILLISECONDS);
		
		scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {

			@Override
			public void run() {
				Date tillDate = new DateTime().minusMinutes(WEBSOCKET_TOLERENCE).toDate();
				for (Iterator<Map.Entry<PageKey, Date>> it = recentRenderedPages.entrySet().iterator(); it.hasNext();) {
					Map.Entry<PageKey, Date> entry = it.next();
					if (entry.getValue().getTime() < tillDate.getTime())
						it.remove();
				}
				for (Iterator<RecentRenderRequest> it = recentRenderRequests.iterator(); it.hasNext();) {
					if (it.next().getDate().getTime() < tillDate.getTime())
						it.remove();
				}
			}
			
		}, 0, WEBSOCKET_TOLERENCE, TimeUnit.MINUTES);
	}

	@Override
	public void stop() {
		scheduledExecutorService.shutdown();
	}
	
}
