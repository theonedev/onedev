package io.onedev.server.terminal;

import java.io.IOException;
import java.util.concurrent.Future;

import io.onedev.commons.utils.command.ExposeOutputStream;
import io.onedev.commons.utils.command.PtyMode;

public class TerminalSession {

	private final Future<?> shellFuture;
	
	private final PtyMode ptyMode;
	
	private final ExposeOutputStream shellInput;
	
	private final Long buildId;
	
	public TerminalSession(Future<?> shellFuture, PtyMode ptyMode, ExposeOutputStream shellInput, Long buildId) {
		this.shellFuture = shellFuture;
		this.ptyMode = ptyMode;
		this.shellInput = shellInput;
		this.buildId = buildId;
	}

	public Future<?> getShellFuture() {
		return shellFuture;
	}

	public PtyMode getPtyMode() {
		return ptyMode;
	}

	public ExposeOutputStream getShellInput() {
		return shellInput;
	}

	public Long getBuildId() {
		return buildId;
	}
	
	public void terminate() {
		try {
			shellInput.getOutput().write("exit\n".getBytes());
		} catch (IOException e) {
		}
		shellFuture.cancel(true);
	}
}
