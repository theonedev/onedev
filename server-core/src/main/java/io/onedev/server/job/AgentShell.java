package io.onedev.server.job;

import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.websocket.api.Session;

import io.onedev.agent.Message;
import io.onedev.agent.WebsocketUtils;
import io.onedev.agent.shell.ShellInputRequest;
import io.onedev.agent.shell.ShellResizeRequest;
import io.onedev.server.terminal.Shell;
import io.onedev.server.terminal.Terminal;

public class AgentShell implements Shell {

	private final JobTerminal terminal;
	
	private final Session agent;
	
	public AgentShell(JobTerminal terminal, Session agent, String jobToken) {
		this.terminal = terminal;
		this.agent = agent;
		new Message(io.onedev.agent.MessageTypes.SHELL_OPEN, terminal.getSessionId() + ":" + jobToken).sendBy(agent);
	}
	
	public Terminal getTerminal() {
		return terminal;
	}

	@Override
	public void writeToStdin(String data) {
		try {
			WebsocketUtils.call(agent, new ShellInputRequest(terminal.getSessionId(), data), 0);
		} catch (InterruptedException | TimeoutException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void resize(int rows, int cols) {
		try {
			WebsocketUtils.call(agent, new ShellResizeRequest(terminal.getSessionId(), rows, cols), 0);
		} catch (InterruptedException | TimeoutException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void terminate() {
		new Message(io.onedev.agent.MessageTypes.SHELL_TERMINATE, terminal.getSessionId()).sendBy(agent);
	}

}
