package com.pmease.commons.loader;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Singleton;

@Singleton
public class DefaultListenerRegistry implements ListenerRegistry {

	private volatile Map<Object, Collection<Method>> listenerMethods;
	
	private Map<Class<?>, Collection<Listener>> listeners = new ConcurrentHashMap<>();
	
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
	
	private Collection<Listener> getListeners(Class<?> eventType) {
		Collection<Listener> listeners = this.listeners.get(eventType);
		if (listeners == null) {
			listeners = new ArrayList<>();
			for (Map.Entry<Object, Collection<Method>> entry: getListenerMethods().entrySet()) {
				for (Method method: entry.getValue()) {
					Class<?> paramType = method.getParameterTypes()[0];
					if (paramType.isAssignableFrom(eventType))
						listeners.add(new Listener(entry.getKey(), method));
				}
			}
			this.listeners.put(eventType, listeners);
		}
		return listeners;
	}
	
	@Override
	public void post(Object event) {
		for (Listener listener: getListeners(event.getClass())) {
			listener.notify(event);
		}
	}

}
