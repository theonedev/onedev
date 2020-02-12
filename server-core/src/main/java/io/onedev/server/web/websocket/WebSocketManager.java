package io.onedev.server.web.websocket;

import org.apache.wicket.protocol.ws.api.IWebSocketConnection;

import io.onedev.server.web.page.base.BasePage;

public interface WebSocketManager {
	
	static final String ERROR_MESSAGE = "ErrorMessage";
	
	static final String OBSERVABLE_CHANGED = "ObservableChanged";
	
	static final String KEEP_ALIVE = "KeepAlive";
	
	void observe(BasePage page);
	
	void onDestroySession(String sessionId);
	
	void notifyObservableChange(String observable);
	
	void onConnect(IWebSocketConnection connection);
}
