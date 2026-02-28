package io.onedev.server.job;

import java.io.Serializable;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.terminal.Shell;

public interface JobRunnable extends Serializable {
	
	boolean run(TaskLogger jobLogger);

	void resume(JobContext jobContext);

	Shell openShell(JobContext jobContext, JobTerminal terminal);
	
}
