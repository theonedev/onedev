package io.onedev.server.web.websocket;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
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
import org.joda.time.DateTime;
import org.quartz.ScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.util.schedule.SchedulableTask;
import io.onedev.server.util.schedule.TaskScheduler;
import io.onedev.server.web.page.base.BasePage;

@Singleton
public class DefaultWebSocketManager implements WebSocketManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultWebSocketManager.class);
	
	private final Application application;
	
	private final TransactionManager transactionManager;
	
	private final WebSocketPolicy webSocketPolicy;
	
	private final TaskScheduler taskScheduler;
	
	private final ExecutorService executorService;
	
	private final Map<String, Map<IKey, Collection<String>>> registeredObservables = new ConcurrentHashMap<>();
	
	private final IWebSocketConnectionRegistry connectionRegistry = new SimpleWebSocketConnectionRegistry();
	
	private final Map<String, Date> notifiedObservables = new ConcurrentHashMap<>();
	
	private String keepAliveTaskId;

	private String notifiedObservableCleanupTaskId;
	
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
			Map<IKey, Collection<String>> sessionPages = registeredObservables.get(sessionId);
			if (sessionPages == null) {
				sessionPages = new ConcurrentHashMap<>();
				registeredObservables.put(sessionId, sessionPages);
			}
			IKey pageKey = new PageIdKey(page.getPageId());
			Collection<String> observables = page.findWebSocketObservables();
			Collection<String> prevObservables = sessionPages.put(pageKey, observables);
			if (prevObservables != null && !prevObservables.containsAll(observables)) {
				IWebSocketConnection connection = connectionRegistry.getConnection(application, sessionId, pageKey);
				if (connection != null)
					notifyPastObservables(connection);
			}
		}
	}
	
	@Override
	public void onDestroySession(String sessionId) {
		registeredObservables.remove(sessionId);
	}
	
	@Nullable
	private Collection<String> getRegisteredObservables(IWebSocketConnection connection) {
		PageKey pageKey = ((WebSocketConnection) connection).getPageKey();
		if (connection.isOpen()) {
			Map<IKey, Collection<String>> sessionPages = registeredObservables.get(pageKey.getSessionId());
			if (sessionPages != null) 
				return sessionPages.get(pageKey.getPageId());
			else
				return null;
		} else {
			return null;
		}
	}
	
	private void notifyObservables(IWebSocketConnection connection, Collection<String> observables) {
		String message = OBSERVABLE_CHANGED + ":" + StringUtils.join(observables, "\n"); 
		try {
			connection.sendMessage(message);
		} catch (Exception e) {
			logger.error("Error sending websocket message: " + message, e);
		}
	}

	@Sessional
	@Override
	public void notifyObservableChange(String observable) {
		transactionManager.runAfterCommit(new Runnable() {

			@Override
			public void run() {
				executorService.execute(new Runnable() {

					@Override
					public void run() {
						notifiedObservables.put(observable, new Date());
						for (IWebSocketConnection connection: connectionRegistry.getConnections(application)) {
							Collection<String> registeredObservables = getRegisteredObservables(connection); 
							if (registeredObservables != null && registeredObservables.contains(observable))
								notifyObservables(connection, Sets.newHashSet(observable));
						}
					}
					
				});
			}
			
		});
	}
	
	@Listen
	public void on(SystemStarted event) {
		keepAliveTaskId = taskScheduler.schedule(new SchedulableTask() {
			
			@Override
			public ScheduleBuilder<?> getScheduleBuilder() {
				return SimpleScheduleBuilder.repeatSecondlyForever((int)webSocketPolicy.getIdleTimeout()/2000);
			}
			
			@Override
			public void execute() {
				for (IWebSocketConnection connection: connectionRegistry.getConnections(application)) {
					if (connection.isOpen()) {
						try {
							connection.sendMessage(WebSocketManager.KEEP_ALIVE);
						} catch (Exception e) {
							logger.error("Error sending websocket keep alive message", e);
						}
					}
				}
			}
			
		});
		notifiedObservableCleanupTaskId = taskScheduler.schedule(new SchedulableTask() {
			
			private static final int TOLERATE_SECONDS = 10;
			
			@Override
			public ScheduleBuilder<?> getScheduleBuilder() {
				return SimpleScheduleBuilder.repeatSecondlyForever(TOLERATE_SECONDS);
			}
			
			@Override
			public void execute() {
				Date threshold = new DateTime().minusSeconds(TOLERATE_SECONDS).toDate();
				for (Iterator<Map.Entry<String, Date>> it = notifiedObservables.entrySet().iterator(); it.hasNext();) {
					if (it.next().getValue().before(threshold))
						it.remove();
				}
			}
			
		});
	}

	@Listen
	public void on(SystemStopping event) {
		taskScheduler.unschedule(keepAliveTaskId);
		taskScheduler.unschedule(notifiedObservableCleanupTaskId);
	}
	
	/**
	 * Since we often re-create pages and re-establish web socket connections. 
	 * Some websocket notifications sent after web page is rendered and before 
	 * connection is available might get lost to cause some states in page never 
	 * gets updated. This mechanism replays a short past observables to new 
	 * connections to avoid the problem  
	 */
	@Override
	public void onConnect(IWebSocketConnection connection) {
		notifyPastObservables(connection);
	}

	private void notifyPastObservables(IWebSocketConnection connection) {
		Collection<String> registeredObservables = getRegisteredObservables(connection);
		if (registeredObservables != null) {
			Set<String> observables = new HashSet<>();
			for (String observable: notifiedObservables.keySet()) {
				if (registeredObservables.contains(observable))
					observables.add(observable);
			}
			if (!observables.isEmpty())
				notifyObservables(connection, observables);
		}
	}

}
