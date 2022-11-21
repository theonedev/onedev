package io.onedev.server.event;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.cluster.ClusterRunnable;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.event.project.ProjectCreated;
import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Transactional;

@Singleton
public class DefaultListenerRegistry implements ListenerRegistry, Serializable {

	private final ProjectManager projectManager;
	
	private final TransactionManager transactionManager;
	
	private final SessionManager sessionManager;
	
	private volatile Map<Object, Collection<Method>> listenerMethods;
	
	private final Map<Class<?>, List<Listener>> listeners = new ConcurrentHashMap<>();
	
	@Inject
	public DefaultListenerRegistry(ProjectManager projectManager, 
			TransactionManager transactionManager, SessionManager sessionManager) {
		this.projectManager = projectManager;
		this.transactionManager = transactionManager;
		this.sessionManager = sessionManager;
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(ListenerRegistry.class);
	}

	private Map<Object, Collection<Method>> getListenerMethods() {
		if (this.listenerMethods == null) {
			Map<Object, Collection<Method>> listenersBySingleton = new HashMap<>();
			for (Object singleton: AppLoader.getSingletons()) {
				Collection<Method> listeners = new ArrayList<>();
				Class<?> current = singleton.getClass();
				Set<Class<?>> encounteredEventTypes = new HashSet<>();
				while (current != Object.class) {
					for (Method method: current.getDeclaredMethods()) {
						if (method.isAnnotationPresent(Listen.class)) {
							if (method.getParameterTypes().length != 1) {
								throw new RuntimeException("Listener method " + method + " should have only one parameter");
							}
							Class<?> eventType = method.getParameterTypes()[0];
							if (!encounteredEventTypes.contains(eventType)) {
								listeners.add(method);
								encounteredEventTypes.add(eventType);
							}
						}
					}
					current = current.getSuperclass();
				}
				listenersBySingleton.put(singleton, listeners);
			}
			this.listenerMethods = listenersBySingleton;
		}
		return this.listenerMethods;
	}
	
	protected Collection<Listener> getListeners(Class<?> eventType) {
		List<Listener> listeners = this.listeners.get(eventType);
		if (listeners == null) {
			listeners = new ArrayList<>();
			for (Map.Entry<Object, Collection<Method>> entry: getListenerMethods().entrySet()) {
				for (Method method: entry.getValue()) {
					Class<?> paramType = method.getParameterTypes()[0];
					if (paramType.isAssignableFrom(eventType))
						listeners.add(new Listener(entry.getKey(), method));
				}
			}
			Collections.sort(listeners);
			this.listeners.put(eventType, listeners);
		}
		return listeners;
	}
	
	@Override
	public void invokeListeners(Object event) {
		for (Listener listener: getListeners(event.getClass())) 
			listener.notify(event);
	}

	@Transactional
	@Override
	public void post(Object event) {
		if (event instanceof ProjectEvent) {
			ProjectEvent projectEvent = (ProjectEvent) event;
			Project project = projectEvent.getProject();
			if (!(event instanceof ProjectCreated)) 
				project.getUpdate().setDate(projectEvent.getDate());
			
			Long projectId = project.getId();
			
			transactionManager.runAfterCommit(new ClusterRunnable() {

				private static final long serialVersionUID = 1L;

				@Override
				public void run() {
					projectManager.submitToProjectServer(projectId, new ClusterTask<Void>() {

						private static final long serialVersionUID = 1L;

						@Override
						public Void call() throws Exception {
							String lockName = projectEvent.getLockName();
							if (lockName != null) {
								LockUtils.call(lockName, true, new Callable<Void>() {

									@Override
									public Void call() throws Exception {
										sessionManager.run(new Runnable() {

											@Override
											public void run() {
												invokeListeners(event);
											}
											
										});
										return null;
									}
									
								});
							} else {
								sessionManager.run(new Runnable() {

									@Override
									public void run() {
										invokeListeners(event);
									}
									
								});
							}
							return null;
						}
						
					});
				}
				
			});
		} else {
			invokeListeners(event);
		}
	}
	
}
