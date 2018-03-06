package io.onedev.server.web.websocket;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.wicket.Application;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.protocol.ws.api.registry.IWebSocketConnectionRegistry;
import org.apache.wicket.protocol.ws.api.registry.PageIdKey;
import org.apache.wicket.protocol.ws.api.registry.SimpleWebSocketConnectionRegistry;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;

import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.web.page.base.BasePage;

@Singleton
public class DefaultWebSocketManager implements WebSocketManager {

	private final Application application;
	
	private final Dao dao;
	
	private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
	
	private final WebSocketPolicy webSocketPolicy;
	
	private final Map<PageKey, Collection<WebSocketRegion>> regions = new ConcurrentHashMap<>();
	
	private final IWebSocketConnectionRegistry connectionRegistry = new SimpleWebSocketConnectionRegistry();

	@Inject
	public DefaultWebSocketManager(Application application, Dao dao, WebSocketPolicy webSocketPolicy) {
		this.application = application;
		this.dao = dao;
		this.webSocketPolicy = webSocketPolicy;
	}
	
	@Override
	public void onRegionChange(BasePage page) {
		String sessionId = page.getSession().getId();
		if (sessionId != null) {
			regions.put(new PageKey(sessionId, new PageIdKey(page.getPageId())), page.getWebSocketRegions());
		}
	}
	
	@Override
	public void onDestroySession(String sessionId) {
		for (Iterator<Map.Entry<PageKey, Collection<WebSocketRegion>>> it = regions.entrySet().iterator(); it.hasNext();) {
			PageKey pageKey = it.next().getKey();
			if (pageKey.getSessionId().equals(sessionId))
				it.remove();
		}
	}

	@Sessional
	@Override
	public void render(WebSocketRegion region, @Nullable PageKey sourcePageKey) {
		dao.doAfterCommit(new Runnable() {

			@Override
			public void run() {
				for (IWebSocketConnection connection: connectionRegistry.getConnections(application)) {
					PageKey pageKey = ((WebSocketConnection) connection).getPageKey();
					if (connection.isOpen() 
							&& (sourcePageKey == null || !sourcePageKey.equals(pageKey)) 
							&& containsRegion(connection, region)) {
						try {
							connection.sendMessage(RENDER_CALLBACK);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				}
			}
			
		});
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
	}

	@Override
	public void stop() {
		scheduledExecutorService.shutdown();
	}

}
