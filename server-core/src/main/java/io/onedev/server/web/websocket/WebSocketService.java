package io.onedev.server.web.websocket;

import java.util.Collection;

import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.jspecify.annotations.Nullable;

import io.onedev.server.web.page.base.BasePage;

public interface WebSocketService {
	
	void observe(BasePage page);
		
	void notifyObservablesChange(Collection<String> observables, @Nullable PageKey sourcePageKey);

	void notifyObservableChange(String observable, @Nullable PageKey sourcePageKey);
	
	void onConnect(IWebSocketConnection connection);
}
