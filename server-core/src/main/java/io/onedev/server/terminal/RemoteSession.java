package io.onedev.server.terminal;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.eclipse.jetty.websocket.api.Session;

import io.onedev.agent.Message;

public class RemoteSession implements ShellSession {

	private static final Map<String, RemoteSession> sessions = new ConcurrentHashMap<>();
	
	private final String sessionId = UUID.randomUUID().toString();
	
	private final IWebSocketConnection connection;
	
	private final Session agentSession;
	
	public RemoteSession(IWebSocketConnection connection, Session agentSession, String jobToken) {
		this.connection = connection;
		this.agentSession = agentSession;

		sessions.put(sessionId, this);
		new Message(io.onedev.agent.MessageTypes.SHELL_OPEN, sessionId + ":" + jobToken).sendBy(agentSession);
	}
	
	public static void sendOutput(String sessionId, String output) {
		RemoteSession session = sessions.get(sessionId);
		if (session != null)
			TerminalUtils.sendOutput(session.connection, output);
	}
	
	public static void sendError(String sessionId, String error) {
		RemoteSession session = sessions.get(sessionId);
		if (session != null)
			TerminalUtils.sendError(session.connection, error);
	}
	
	public static void onRemoteClosed(String sessionId) {
		RemoteSession session = sessions.remove(sessionId);
		if (session != null)
			TerminalUtils.close(session.connection);
	}
	
	@Override
	public void sendInput(String input) {
		new Message(io.onedev.agent.MessageTypes.SHELL_INPUT, sessionId + ":" + input).sendBy(agentSession);
	}

	@Override
	public void resize(int rows, int cols) {
		new Message(io.onedev.agent.MessageTypes.SHELL_RESIZE, sessionId + ":" + rows + ":" + cols).sendBy(agentSession);
	}

	@Override
	public void exit() {
		sessions.remove(sessionId);
		TerminalUtils.sendError(connection, "Shell exited");
		new Message(io.onedev.agent.MessageTypes.SHELL_EXIT, sessionId).sendBy(agentSession);
	}

}
