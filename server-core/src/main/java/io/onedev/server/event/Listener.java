package io.onedev.server.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Listener {

	private final Object singleton;
	
	private final Method method;
	
	public Listener(Object singleton, Method method) {
		this.singleton = singleton;
		this.method = method;
	}

	public Object notify(Object event) {
		try {
			return method.invoke(singleton, event);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
}
