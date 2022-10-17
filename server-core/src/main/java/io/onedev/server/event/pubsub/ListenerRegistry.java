package io.onedev.server.event.pubsub;

public interface ListenerRegistry {
	
	void post(Object event);
	
	void invokeListeners(Object event);
	
}
