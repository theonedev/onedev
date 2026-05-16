package io.onedev.server.workspace;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.SerializationUtils;
import org.eclipse.jetty.websocket.api.Session;
import org.jspecify.annotations.Nullable;

import io.onedev.agent.Message;
import io.onedev.agent.MessageTypes;
import io.onedev.agent.shell.WorkspaceShellInputRequest;
import io.onedev.agent.shell.WorkspaceShellResizeRequest;
import io.onedev.agent.workspace.WorkspaceShellOpenData;
import io.onedev.server.terminal.Shell;
import io.onedev.server.terminal.Terminal;

public class WorkspaceAgentShell implements Shell {
	
	private static final Map<String, WorkspaceAgentShell> shells = new ConcurrentHashMap<>();

	private final String sessionId;

	private final Terminal terminal;

	private final Session agent;

	public WorkspaceAgentShell(Terminal terminal, Session agent, WorkspaceShellOpenData data) {
		this.terminal = terminal;
		this.agent = agent;
		sessionId = data.getSessionId();
		shells.put(sessionId, this);
		new Message(MessageTypes.WORKSPACE_SHELL_OPEN, SerializationUtils.serialize(data)).sendBy(agent);
	}

	@Nullable
	public static WorkspaceAgentShell get(String sessionId) {
		return shells.get(sessionId);
	}

	public Terminal getTerminal() {
		return terminal;
	}

	public String getSessionId() {
		return sessionId;
	}

	@Override
	public void writeToStdin(String data) {
		new Message(MessageTypes.WORKSPACE_SHELL_INPUT, 
				new WorkspaceShellInputRequest(sessionId, data)).sendBy(agent);
	}

	@Override
	public void resize(int rows, int cols) {
		new Message(MessageTypes.WORKSPACE_SHELL_RESIZE, 
				new WorkspaceShellResizeRequest(sessionId, rows, cols)).sendBy(agent);
	}

	@Override
	public void terminate() {
		shells.remove(sessionId);
		new Message(MessageTypes.WORKSPACE_SHELL_TERMINATE, sessionId).sendBy(agent);
	}

}
