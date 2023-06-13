package io.onedev.server.web.websocket;

import org.apache.wicket.protocol.ws.api.IWebSocketConnection;

import io.onedev.server.web.page.base.BasePage;

import javax.annotation.Nullable;
import java.util.Collection;

public interface WebSocketManager {
	
	void observe(BasePage page);
	
	void onDestroySession(String sessionId);
	
	void notifyObservablesChange(Collection<String> observables, @Nullable PageKey sourcePageKey);

	void notifyObservableChange(String observable, @Nullable PageKey sourcePageKey);
	
	void onConnect(IWebSocketConnection connection);
}
