package io.onedev.server.ee.terminal;

import io.onedev.server.terminal.Shell;

public class TerminalSession {

	private final Long buildId;
	
	private final Shell shell;
	
	public TerminalSession(Long buildId, Shell shellSession) {
		this.buildId = buildId;
		this.shell = shellSession;
	}

	public Long getBuildId() {
		return buildId;
	}

	public Shell getShell() {
		return shell;
	}
	
}
