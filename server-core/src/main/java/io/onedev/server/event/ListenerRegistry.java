package io.onedev.server.event;

public interface ListenerRegistry {
	
	void post(Object event);
	
	void invokeListeners(Object event);
	
}
