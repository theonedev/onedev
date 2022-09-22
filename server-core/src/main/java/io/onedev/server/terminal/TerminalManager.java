package io.onedev.server.terminal;

import org.apache.wicket.protocol.ws.api.IWebSocketConnection;

import io.onedev.server.model.Build;

public interface TerminalManager {
		
	void onOpen(IWebSocketConnection connection, Build build);
	
	void onClose(IWebSocketConnection connection);
	
	void onResize(IWebSocketConnection connection, int rows, int cols);
	
	void onInput(IWebSocketConnection connection, String input);
	
}
