package io.onedev.server.web.websocket;

import com.google.common.collect.Sets;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.cluster.ClusterRunnable;
import io.onedev.server.event.Listen;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.util.Pair;
import io.onedev.server.taskschedule.SchedulableTask;
import io.onedev.server.taskschedule.TaskScheduler;
import io.onedev.server.web.page.base.BasePage;
import org.apache.wicket.Application;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.protocol.ws.api.registry.IKey;
import org.apache.wicket.protocol.ws.api.registry.IWebSocketConnectionRegistry;
import org.apache.wicket.protocol.ws.api.registry.PageIdKey;
import org.apache.wicket.protocol.ws.api.registry.SimpleWebSocketConnectionRegistry;
import org.joda.time.DateTime;
import org.quartz.ScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.onedev.server.web.behavior.ChangeObserver.containsObservable;
import static io.onedev.server.web.behavior.ChangeObserver.filterObservables;

@Singleton
public class DefaultWebSocketManager implements WebSocketManager, Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(DefaultWebSocketManager.class);
	
	private static final int KEEP_ALIVE_INTERVAL = 30;
	
	private final Application application;
	
	private final TransactionManager transactionManager;
	
	private final TaskScheduler taskScheduler;
	
	private final ClusterManager clusterManager;
	
	private final Map<String, Map<IKey, Collection<String>>> registeredObservables = new ConcurrentHashMap<>();
	
	private final IWebSocketConnectionRegistry connectionRegistry = new SimpleWebSocketConnectionRegistry();
	
	private final Map<String, Pair<PageKey, Date>> notifiedObservables = new ConcurrentHashMap<>();
	
	private String keepAliveTaskId;

	private String notifiedObservableCleanupTaskId;
	
	@Inject
	public DefaultWebSocketManager(Application application, TransactionManager transactionManager, 
			TaskScheduler taskScheduler, ClusterManager clusterManager) {
		this.application = application;
		this.transactionManager = transactionManager;
		this.taskScheduler = taskScheduler;
		this.clusterManager = clusterManager;
	}
	
	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(WebSocketManager.class);
	}
	
	@Override
	public void observe(BasePage page) {
		String sessionId = page.getSession().getId();
		if (sessionId != null) {
			Map<IKey, Collection<String>> observablesOfSession = registeredObservables.get(sessionId);
			if (observablesOfSession == null) {
				observablesOfSession = new ConcurrentHashMap<>();
				registeredObservables.put(sessionId, observablesOfSession);
			}
			IKey pageKey = new PageIdKey(page.getPageId());
			Collection<String> observables = page.findChangeObservables();
			Collection<String> prevObservables = observablesOfSession.put(pageKey, observables);
			if (prevObservables != null && !observables.stream().allMatch(it -> prevObservables.stream().anyMatch(it2 -> containsObservable(it2, it)))) {
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
		if (connection.isOpen()) {
			PageKey pageKey = ((WebSocketConnection) connection).getPageKey();
			Map<IKey, Collection<String>> observablesOfSession = registeredObservables.get(pageKey.getSessionId());
			if (observablesOfSession != null) 
				return observablesOfSession.get(pageKey.getPageId());
			else
				return null;
		} else {
			return null;
		}
	}
	
	private void notifyObservables(IWebSocketConnection connection, Collection<String> observables) {
		String message = WebSocketMessages.OBSERVABLE_CHANGED + ":" + StringUtils.join(observables, "\n"); 
		try {
			connection.sendMessage(message);
		} catch (Exception e) {
			logger.error("Error sending websocket message: " + message, e);
		}
	}

	@Override
	public void notifyObservableChange(String observable, PageKey sourcePageKey) {
		notifyObservablesChange(Sets.newHashSet(observable), sourcePageKey);
	}

	@Sessional
	@Override
	public void notifyObservablesChange(Collection<String> observables, @Nullable PageKey sourcePageKey) {
		transactionManager.runAfterCommit(new ClusterRunnable() {

			private static final long serialVersionUID = 1L;

			@Override
			public void run() {
				clusterManager.submitToAllServers(() -> {
					for (var observable: observables)
						notifiedObservables.put(observable, new Pair<>(sourcePageKey, new Date()));
					for (IWebSocketConnection connection: connectionRegistry.getConnections(application)) {
						PageKey pageKey = ((WebSocketConnection) connection).getPageKey();
						if (sourcePageKey == null || !sourcePageKey.equals(pageKey)) {
							Collection<String> registeredObservables = getRegisteredObservables(connection);
							if (registeredObservables != null) {
								var registeredChangedObservables = 
										filterObservables(registeredObservables, observables);
								if (!registeredChangedObservables.isEmpty())
									notifyObservables(connection, registeredChangedObservables);
							}
						}
					}
					return null;
				});
			}
			
		});
	}
	
	@Listen
	public void on(SystemStarted event) {
		keepAliveTaskId = taskScheduler.schedule(new SchedulableTask() {
			
			@Override
			public ScheduleBuilder<?> getScheduleBuilder() {
				return SimpleScheduleBuilder.repeatSecondlyForever(KEEP_ALIVE_INTERVAL);
			}
			
			@Override
			public void execute() {
				for (IWebSocketConnection connection: connectionRegistry.getConnections(application)) {
					if (connection.isOpen()) {
						try {
							connection.sendMessage(WebSocketMessages.KEEP_ALIVE);
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
				for (var it = notifiedObservables.entrySet().iterator(); it.hasNext();) {
					if (it.next().getValue().getRight().before(threshold))
						it.remove();
				}
			}
			
		});
	}

	@Listen
	public void on(SystemStopping event) {
		if (keepAliveTaskId != null)
			taskScheduler.unschedule(keepAliveTaskId);
		if (notifiedObservableCleanupTaskId != null)
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
		PageKey pageKey = ((WebSocketConnection) connection).getPageKey();
		Collection<String> registeredObservables = getRegisteredObservables(connection);
		if (registeredObservables != null) {
			Set<String> observables = new HashSet<>();
			for (var entry: notifiedObservables.entrySet()) {
				var observable = entry.getKey();
				var sourcePageKey = entry.getValue().getLeft();
				if ((sourcePageKey == null || !sourcePageKey.equals(pageKey)) 
						&& registeredObservables.stream().anyMatch(it-> containsObservable(it, observable))) {
					observables.add(observable);
				}
			}
			if (!observables.isEmpty())
				notifyObservables(connection, observables);
		}
	}

}
