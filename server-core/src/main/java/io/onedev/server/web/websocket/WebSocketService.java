package io.onedev.server.web.websocket;

import java.util.Collection;

import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.jspecify.annotations.Nullable;

import io.onedev.server.web.page.base.BasePage;

public interface WebSocketService {
	
	// Using 30 seconds to avoid Nginx websocket timeout (60 seconds by default)
	static final int KEEP_ALIVE_INTERVAL = 30;

	void observe(BasePage page);
		
	void notifyObservablesChange(Collection<String> observables, @Nullable PageKey sourcePageKey);

	void notifyObservableChange(String observable, @Nullable PageKey sourcePageKey);
	
	void onConnect(IWebSocketConnection connection);
}
