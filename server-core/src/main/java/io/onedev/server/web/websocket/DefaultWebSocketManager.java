package io.onedev.server.web.websocket;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.wicket.Application;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.protocol.ws.api.registry.IKey;
import org.apache.wicket.protocol.ws.api.registry.IWebSocketConnectionRegistry;
import org.apache.wicket.protocol.ws.api.registry.PageIdKey;
import org.apache.wicket.protocol.ws.api.registry.SimpleWebSocketConnectionRegistry;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.quartz.ScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.util.schedule.SchedulableTask;
import io.onedev.server.util.schedule.TaskScheduler;
import io.onedev.server.web.page.base.BasePage;

@Singleton
public class DefaultWebSocketManager implements WebSocketManager, SchedulableTask {

	private static final Logger logger = LoggerFactory.getLogger(DefaultWebSocketManager.class);
	
	private final Application application;
	
	private final TransactionManager transactionManager;
	
	private final WebSocketPolicy webSocketPolicy;
	
	private final TaskScheduler taskScheduler;
	
	private final ExecutorService executorService;
	
	private final Map<String, Map<IKey, Collection<String>>> observables = new ConcurrentHashMap<>();
	
	private final IWebSocketConnectionRegistry connectionRegistry = new SimpleWebSocketConnectionRegistry();
	
	private String taskId;

	@Inject
	public DefaultWebSocketManager(Application application, TransactionManager transactionManager, 
			WebSocketPolicy webSocketPolicy, TaskScheduler taskScheduler, ExecutorService executorService) {
		this.application = application;
		this.transactionManager = transactionManager;
		this.webSocketPolicy = webSocketPolicy;
		this.taskScheduler = taskScheduler;
		this.executorService = executorService;
	}
	
	@Override
	public void observe(BasePage page) {
		String sessionId = page.getSession().getId();
		if (sessionId != null) {
			Map<IKey, Collection<String>> sessionPages = observables.get(sessionId);
			if (sessionPages == null) {
				sessionPages = new ConcurrentHashMap<>();
				observables.put(sessionId, sessionPages);
			}
			sessionPages.put(new PageIdKey(page.getPageId()), page.findWebSocketObservables());
		}
	}
	
	@Override
	public void onDestroySession(String sessionId) {
		observables.remove(sessionId);
	}

	@Sessional
	@Override
	public void notifyObservableChange(String observable, @Nullable PageKey sourcePageKey) {
		transactionManager.runAfterCommit(new Runnable() {

			@Override
			public void run() {
				executorService.execute(new Runnable() {

					@Override
					public void run() {
						for (IWebSocketConnection connection: connectionRegistry.getConnections(application)) {
							PageKey pageKey = ((WebSocketConnection) connection).getPageKey();
							if (connection.isOpen() && (sourcePageKey == null || !sourcePageKey.equals(pageKey))) {
								Map<IKey, Collection<String>> sessionPages = observables.get(pageKey.getSessionId());
								if (sessionPages != null) {
									Collection<String> pageObservables = sessionPages.get(pageKey.getPageId());
									if (pageObservables != null && pageObservables.contains(observable)) {
										String message = OBSERVABLE_CHANGED + ":" + observable; 
										try {
											connection.sendMessage(message);
										} catch (Exception e) {
											logger.error("Error sending websocket message: " + message, e);
										}
									}
								}
							}
						}
					}
					
				});
			}
			
		});
	}
	
	@Override
	public void execute() {
		for (IWebSocketConnection connection: new SimpleWebSocketConnectionRegistry().getConnections(application)) {
			if (connection.isOpen()) {
				try {
					connection.sendMessage(WebSocketManager.KEEP_ALIVE);
				} catch (Exception e) {
					logger.error("Error sending websocket keep alive message", e);
				}
			}
		}
	}
	
	@Listen
	public void on(SystemStarted event) {
		taskId = taskScheduler.schedule(this);
	}

	@Listen
	public void on(SystemStopping event) {
		taskScheduler.unschedule(taskId);
	}
	
	@Override
	public ScheduleBuilder<?> getScheduleBuilder() {
		return SimpleScheduleBuilder.repeatSecondlyForever((int)webSocketPolicy.getIdleTimeout()/2000);
	}

}
