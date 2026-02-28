package io.onedev.server.job;

import io.onedev.server.model.Build;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;

public interface JobTerminalService {
	
	String getTerminalUrl(Build build);
	
	void onOpen(IWebSocketConnection connection, Build build);
	
	void onClose(IWebSocketConnection connection);
	
	void onResized(IWebSocketConnection connection, int rows, int cols);
	
	void writeToStdin(IWebSocketConnection connection, String data);
	
	void terminateShells(Long buildId);

}
