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
	
	private final Map<PageKey, Collection<String>> observables = new ConcurrentHashMap<>();
	
	private final IWebSocketConnectionRegistry connectionRegistry = new SimpleWebSocketConnectionRegistry();

	@Inject
	public DefaultWebSocketManager(Application application, Dao dao, WebSocketPolicy webSocketPolicy) {
		this.application = application;
		this.dao = dao;
		this.webSocketPolicy = webSocketPolicy;
	}
	
	@Override
	public void notifyObserverChange(BasePage page) {
		String sessionId = page.getSession().getId();
		if (sessionId != null) {
			observables.put(new PageKey(sessionId, new PageIdKey(page.getPageId())), page.findWebSocketObservables());
		}
	}
	
	@Override
	public void onDestroySession(String sessionId) {
		for (Iterator<Map.Entry<PageKey, Collection<String>>> it = observables.entrySet().iterator(); it.hasNext();) {
			PageKey pageKey = it.next().getKey();
			if (pageKey.getSessionId().equals(sessionId))
				it.remove();
		}
	}

	@Sessional
	@Override
	public void notifyObservableChange(String observable, @Nullable PageKey sourcePageKey) {
		dao.doAfterCommit(new Runnable() {

			@Override
			public void run() {
				for (IWebSocketConnection connection: connectionRegistry.getConnections(application)) {
					PageKey pageKey = ((WebSocketConnection) connection).getPageKey();
					if (connection.isOpen() && (sourcePageKey == null || !sourcePageKey.equals(pageKey))) {
						Collection<String> pageObservables = observables.get(pageKey);
						if (pageObservables != null && pageObservables.contains(observable)) {
							try {
								connection.sendMessage(OBSERVABLE_CHANGED + ":" + observable);
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						}
					}
				}
			}
			
		});
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
