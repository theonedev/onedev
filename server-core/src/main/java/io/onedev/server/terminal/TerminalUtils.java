package io.onedev.server.terminal;

import org.apache.wicket.protocol.ws.api.IWebSocketConnection;

public class TerminalUtils {
	
	public static void sendOutput(IWebSocketConnection connection, String output) {
		try {
			connection.sendMessage(MessageTypes.TERMINAL_OUTPUT + ":" + output);
		} catch (Exception e) {
		}
	}
	
	public static void sendError(IWebSocketConnection connection, String error) {
		try {
			connection.sendMessage(MessageTypes.TERMINAL_OUTPUT + ":\r\n\033[31m" + error + "\033[0m");
		} catch (Exception e) {
		}
	}

	public static void close(IWebSocketConnection connection) {
		try {
			connection.sendMessage(MessageTypes.TERMINAL_CLOSE.name());
		} catch (Exception e) {
		}
	}

}
