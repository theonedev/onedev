package io.onedev.server.web.websocket;

import javax.annotation.Nullable;

import io.onedev.server.web.page.base.BasePage;

public interface WebSocketManager {
	
	static final String ERROR_MESSAGE = "ErrorMessage";
	
	static final String OBSERVABLE_CHANGED = "ObservableChanged";
	
	static final String CONNECTION_OPENED = "ConnectionOpened";
	
	static final String KEEP_ALIVE = "KeepAlive";
	
	void notifyObserverChange(BasePage page);
	
	void onDestroySession(String sessionId);
	
	void notifyObservableChange(String observable, @Nullable PageKey sourcePageKey);
	
	void start();
	
	void stop();
	
}
