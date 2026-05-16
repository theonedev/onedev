package io.onedev.server.job;

import org.apache.commons.lang3.SerializationUtils;
import org.eclipse.jetty.websocket.api.Session;

import io.onedev.agent.Message;
import io.onedev.agent.MessageTypes;
import io.onedev.agent.shell.JobShellInputRequest;
import io.onedev.agent.shell.JobShellOpenData;
import io.onedev.agent.shell.JobShellResizeRequest;
import io.onedev.server.terminal.Shell;
import io.onedev.server.terminal.Terminal;

public class JobAgentShell implements Shell {

	private final JobTerminal terminal;
	
	private final Session agent;
	
	public JobAgentShell(JobTerminal terminal, Session agent, JobShellOpenData data) {
		this.terminal = terminal;
		this.agent = agent;
		new Message(MessageTypes.JOB_SHELL_OPEN, SerializationUtils.serialize(data)).sendBy(agent);
	}
	
	public Terminal getTerminal() {
		return terminal;
	}

	@Override
	public void writeToStdin(String data) {
		new Message(MessageTypes.JOB_SHELL_INPUT, 
				new JobShellInputRequest(terminal.getSessionId(), data)).sendBy(agent);
	}

	@Override
	public void resize(int rows, int cols) {
		new Message(MessageTypes.JOB_SHELL_RESIZE, 
				new JobShellResizeRequest(terminal.getSessionId(), rows, cols)).sendBy(agent);
	}

	@Override
	public void terminate() {
		new Message(MessageTypes.JOB_SHELL_TERMINATE, terminal.getSessionId()).sendBy(agent);
	}

}
