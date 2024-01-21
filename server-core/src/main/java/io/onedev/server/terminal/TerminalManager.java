package io.onedev.server.terminal;

import io.onedev.server.model.Build;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;

public interface TerminalManager {
	
	String getTerminalUrl(Build build);
	
	void onOpen(IWebSocketConnection connection, Build build);
	
	void onClose(IWebSocketConnection connection);
	
	void onResize(IWebSocketConnection connection, int rows, int cols);
	
	void onInput(IWebSocketConnection connection, String input);
	
}
