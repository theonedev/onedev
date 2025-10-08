package io.onedev.server.web.websocket;

import org.apache.wicket.protocol.ws.api.IWebSocketConnection;

import io.onedev.server.web.page.base.BasePage;

import org.jspecify.annotations.Nullable;
import java.util.Collection;

public interface WebSocketService {
	
	void observe(BasePage page);
	
	void onDestroySession(String sessionId);
	
	void notifyObservablesChange(Collection<String> observables, @Nullable PageKey sourcePageKey);

	void notifyObservableChange(String observable, @Nullable PageKey sourcePageKey);
	
	void onConnect(IWebSocketConnection connection);
}
