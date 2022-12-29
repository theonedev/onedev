package io.onedev.server.job;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.terminal.Shell;
import io.onedev.server.terminal.Terminal;

import java.io.Serializable;

public interface JobRunnable extends Serializable {
	
	void run(TaskLogger jobLogger);

	void resume(JobContext jobContext);

	Shell openShell(JobContext jobContext, Terminal terminal);
	
}
