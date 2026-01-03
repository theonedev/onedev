package io.onedev.server.web.websocket;

import static io.onedev.server.web.behavior.ChangeObserver.containsObservable;
import static io.onedev.server.web.behavior.ChangeObserver.filterObservables;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.wicket.Application;
import org.apache.wicket.protocol.ws.WebSocketSettings;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.protocol.ws.api.registry.IKey;
import org.apache.wicket.protocol.ws.api.registry.IWebSocketConnectionRegistry;
import org.apache.wicket.protocol.ws.api.registry.PageIdKey;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.joda.time.DateTime;
import org.jspecify.annotations.Nullable;
import org.quartz.ScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;

import com.google.common.collect.Sets;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.server.cluster.ClusterRunnable;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.event.Listen;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.taskschedule.SchedulableTask;
import io.onedev.server.taskschedule.TaskScheduler;
import io.onedev.server.util.Pair;
import io.onedev.server.web.SessionListener;
import io.onedev.server.web.page.base.BasePage;

@Singleton
public class DefaultWebSocketService implements WebSocketService, SessionListener, Serializable, SchedulableTask {

	private static final long serialVersionUID = 1L;

	private static final int NOTIFY_TOLERATE_SECONDS = 5;

	private static final int CHECK_MESSAGE_QUEUE_INTERVAL = 5;

	@Inject
	private Application application;

	@Inject
	private TransactionService transactionService;

	@Inject
	private TaskScheduler taskScheduler;

	@Inject
	private ClusterService clusterService;
	
	private final Map<String, Map<IKey, Collection<String>>> registeredObservables = new ConcurrentHashMap<>();
		
	private final Map<String, Pair<PageKey, Date>> notifiedObservables = new ConcurrentHashMap<>();

	private volatile IWebSocketConnectionRegistry connectionRegistry;
	
	private volatile String taskId;

	private volatile Thread checkMessageQueueThread;

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(WebSocketService.class);
	}

	private IWebSocketConnectionRegistry getConnectionRegistry() {
		if (connectionRegistry == null) 
			connectionRegistry = WebSocketSettings.Holder.get(application).getConnectionRegistry();
		return connectionRegistry;
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
				IWebSocketConnection connection = getConnectionRegistry().getConnection(application, sessionId, pageKey);
				if (connection != null)
					notifyPastObservables(connection);
			}
		}
	}
	
	@Override
	public void sessionCreated(String sessionId) {
	}
	
	@Override
	public void sessionDestroyed(String sessionId) {
		for (IWebSocketConnection connection : getConnectionRegistry().getConnections(application, sessionId)) {
			try {
				connection.close(StatusCode.NORMAL, "Session destroyed");
				IKey pageKey = ((WebSocketConnection) connection).getPageKey().getPageId();
				getConnectionRegistry().removeConnection(application, sessionId, pageKey);
			} catch (Exception e) {
			}
		}
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
	
	private void notifyObservablesChange(IWebSocketConnection connection, Set<String> observables) {
		if (((WebSocketConnection) connection).queueMessage(new ObservablesChanged(observables))) {
			var thread = checkMessageQueueThread;
			if (thread != null) 
				thread.interrupt();	
		}
	}

	@Override
	public void notifyObservableChange(String observable, PageKey sourcePageKey) {
		notifyObservablesChange(Sets.newHashSet(observable), sourcePageKey);
	}

	@Sessional
	@Override
	public void notifyObservablesChange(Collection<String> observables, @Nullable PageKey sourcePageKey) {
		transactionService.runAfterCommit(new ClusterRunnable() {

			private static final long serialVersionUID = 1L;

			@Override
			public void run() {
				clusterService.submitToAllServers(() -> {
					for (var observable: observables)
						notifiedObservables.put(observable, new Pair<>(sourcePageKey, new Date()));
					for (IWebSocketConnection connection: getConnectionRegistry().getConnections(application)) {
						PageKey pageKey = ((WebSocketConnection) connection).getPageKey();
						if (sourcePageKey == null || !sourcePageKey.equals(pageKey)) {
							Collection<String> registeredObservables = getRegisteredObservables(connection);
							if (registeredObservables != null) {
								var registeredChangedObservables = 
										filterObservables(registeredObservables, observables);
								if (!registeredChangedObservables.isEmpty())
									notifyObservablesChange(connection, registeredChangedObservables);
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
		taskId = taskScheduler.schedule(this);

		checkMessageQueueThread = new Thread(() -> {
			while (checkMessageQueueThread != null) {
				for (var connection: getConnectionRegistry().getConnections(application)) {
					if (connection.isOpen()) 
						((WebSocketConnection) connection).checkMessageQueue();
				}
				try {
					Thread.sleep(CHECK_MESSAGE_QUEUE_INTERVAL * 1000);
				} catch (InterruptedException e) {	
				}		
			}
		});
		checkMessageQueueThread.start();
	}

	@Listen
	public void on(SystemStopping event) {
		var thread = checkMessageQueueThread;
		checkMessageQueueThread = null;
		if (thread != null) 
			thread.interrupt();		
		if (taskId != null)
			taskScheduler.unschedule(taskId);
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
				notifyObservablesChange(connection, observables);
		}
	}

	@Override
	public ScheduleBuilder<?> getScheduleBuilder() {
		return SimpleScheduleBuilder.repeatSecondlyForever(NOTIFY_TOLERATE_SECONDS);
	}
	
	@Override
	public void execute() {
		Date threshold = new DateTime().minusSeconds(NOTIFY_TOLERATE_SECONDS).toDate();
		for (var it = notifiedObservables.entrySet().iterator(); it.hasNext();) {
			if (it.next().getValue().getRight().before(threshold))
				it.remove();
		}
	}

}
